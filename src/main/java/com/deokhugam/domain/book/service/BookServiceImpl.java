package com.deokhugam.domain.book.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.mapper.BookMapper;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.storage.FileStorage;
import com.deokhugam.global.storage.exception.S3.S3FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final FileStorage s3Storage;


    @Override
    @Transactional(readOnly = true)
    public BookDto getBookDetail(UUID bookId) {
        return bookRepository.findBookDetailById(bookId)
                .orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseBookDto searchBooks(BookSearchCondition bookSearchCondition) {
        Pageable pageable = PageRequest.of(0, bookSearchCondition.limit());
        Page<BookDto> pageBook = bookRepository.findBooks(bookSearchCondition, pageable);
        List<BookDto> content = pageBook.getContent();
        BookDto last = content.isEmpty() ? null : content.get(content.size() - 1);
        String nextCursor = null;
        Instant nextAfter = null;
        if (pageBook.hasNext() && last != null) {
            nextCursor = buildNextCursor(bookSearchCondition, last);
            nextAfter = last.createdAt(); // 레포의 tie-breaker와 맞춤
        }
        return new CursorPageResponseBookDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                pageBook.getTotalElements(),
                pageBook.hasNext()
        );
    }

    private String buildNextCursor(BookSearchCondition condition, BookDto last) {
        return switch (condition.orderBy()) {
            case TITLE -> last.title();
            case PUBLISHED_DATE -> last.publishedDate().toString();
            case RATING -> Double.toString(last.rating());
            case REVIEW_COUNT -> Long.toString(last.reviewCount());
        };
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponsePopularBookDto searchPopularBooks(PopularBookSearchCondition popularBookSearchCondition) {
        // TODO: 26. 1. 9. 인기 도서 미구현으로 틀 성생 추후 로직 구현(해당 기능 popularBook 이전 고려 필요)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public NaverBookDto getBookByIsbn(String isbn) {

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public String extractIsbnFromImage(MultipartFile image) {
        return "";
    }

    @Override
    @Transactional
    public BookDto createBook(BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {

        Book savedBook = bookRepository.save(
                Book.create(
                        bookCreateRequest.title(),
                        bookCreateRequest.author(),
                        bookCreateRequest.isbn(),
                        bookCreateRequest.publishedDate(),
                        bookCreateRequest.publisher(),
                        null,
                        bookCreateRequest.description()
                ));

        log.info("책 생성 요청 - 제목: {}, ISBN: {}, 저자: {}",
                bookCreateRequest.title(), bookCreateRequest.isbn(), bookCreateRequest.author());
        if (bookRepository.existsByIsbn(bookCreateRequest.isbn())) {
            log.warn("책 생성 실패: 이미 존재하는 ISBN - {}", bookCreateRequest.isbn());
            throw new BookException(ErrorCode.DUPLICATE_BOOK_ISBN);
        }
        String savedFileKey = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            savedFileKey = s3Storage.upload(thumbnail, savedBook.getId().toString());

            log.debug("썸네일 업로드 완료 - Key: {}", savedFileKey);

            bookCreateFailedRollbackCleanup(savedFileKey);
        }

        savedBook.updateThumbnailUrl(savedFileKey + "?v=" + LocalDateTime.now());

        savedBook = bookRepository.save(savedBook);

        String cdnUrl = s3Storage.generateUrl(savedBook.getThumbnailUrl());

        log.info("책 생성 완료 - ID: {}, 제목: {}", savedBook.getId(), savedBook.getTitle());

        return BookMapper.toDto(savedBook, cdnUrl, 0L, 0.0);
    }
    @Override
    @Transactional
    public BookDto updateBook(UUID bookId, BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));

        if(!existingBook.getIsbn().equals(bookCreateRequest.isbn())&&bookRepository.existsByIsbn(bookCreateRequest.isbn())){
            log.warn("책 수정 실패: 이미 존재하는 ISBN - {}", bookCreateRequest.isbn());
            throw new BookException(ErrorCode.DUPLICATE_BOOK_ISBN);
        }

        String newDbUrl = existingBook.getThumbnailUrl();
        String oldFileKeyToDelete = null;

        if (thumbnail != null && !thumbnail.isEmpty()) {

            String newOriginalFileName = thumbnail.getOriginalFilename();
            String newExt = Objects.requireNonNull(newOriginalFileName).substring(newOriginalFileName.lastIndexOf(".") + 1);
            String newS3Key = "books/" + existingBook.getId() + "." + newExt;

            s3Storage.upload(thumbnail, newS3Key);
            log.info("썸네일 업로드(수정) 완료 - Key: {}", newS3Key);

            bookCreateFailedRollbackCleanup(newS3Key);

            String currentDbUrl = existingBook.getThumbnailUrl();

            if (currentDbUrl != null && !currentDbUrl.isBlank()) {
                String pureOldKey = currentDbUrl.split("\\?")[0];
                if (!pureOldKey.equals(newS3Key)) {
                    oldFileKeyToDelete = pureOldKey;
                }
                newDbUrl = newS3Key + "?v=" + LocalDateTime.now();
            }
        }
        existingBook.update(
                bookCreateRequest.title(),
                bookCreateRequest.author(),
                bookCreateRequest.isbn(),
                bookCreateRequest.publishedDate(),
                bookCreateRequest.publisher(),
                bookCreateRequest.description(),
                newDbUrl // 이미지가 변경되었으면 새 값, 아니면 기존 값
        );

        if (oldFileKeyToDelete != null) {
            try {
                s3Storage.delete(oldFileKeyToDelete);
                log.info("기존 파일 삭제 완료 - Key: {}", oldFileKeyToDelete);
            } catch (Exception e) {
                log.warn("기존 파일 삭제 실패 (고아 객체 확인 필요) - Key: {}", oldFileKeyToDelete, e);
            }
        }
        String cdnUrl = s3Storage.generateUrl(existingBook.getThumbnailUrl());
        log.info("책 수정 완료 - ID: {}", existingBook.getId());

        BookDto dto = getBookDetail(bookId);

        return BookMapper.toDto(existingBook, cdnUrl, dto.reviewCount(), dto.rating());
    }

    @Override
    @Transactional
    public void softDeleteBook(UUID bookId) {
        // TODO: 26. 1. 9. 명확한 구분을 위해 soft, hard로 구분 상의 필요  
    }

    @Override
    @Transactional
    public void hardDeleteBook(UUID bookId) {

    }

    private void bookCreateFailedRollbackCleanup(String fileKey) {
        if (fileKey == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("트랜잭션 롤백 감지: S3 업로드 파일 삭제 시도 - Key: {}", fileKey);
                    s3Storage.delete(fileKey);
                }
            }
        });
    }
}
