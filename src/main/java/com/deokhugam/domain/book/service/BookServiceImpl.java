package com.deokhugam.domain.book.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.mapper.BookMapper;
import com.deokhugam.domain.book.mapper.BookUrlMapper;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.search.book.BookApiManager;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.storage.FileStorage;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final FileStorage fileStorage;
    private final BookApiManager bookApiManager;
    private final BookUrlMapper bookUrlMapper;


    @Override
    @Transactional(readOnly = true)
    public BookDto getBookDetail(UUID bookId) {
        BookDto bookDto = bookRepository.findBookDetailById(bookId)
                .orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));
        return bookUrlMapper.withFullThumbnailUrl(bookDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseBookDto searchBooks(BookSearchCondition bookSearchCondition) {
        Pageable pageable = PageRequest.of(0, bookSearchCondition.limit());
        Page<BookDto> pageBook = bookRepository.findBooks(bookSearchCondition, pageable);
        List<BookDto> content = bookUrlMapper.withFullThumbnailUrl(pageBook.getContent());
        BookDto last = content.isEmpty() ? null : content.get(content.size() - 1);
        String nextCursor = null;
        Instant nextAfter = null;
        if (pageBook.hasNext() && last != null) {
            nextCursor = buildNextCursor(bookSearchCondition, last);
            nextAfter = last.createdAt(); // 레포의 tie-breaker와 맞춤
        }
        log.debug("도서 목록 조회 완료 - limit={}, resultCount={}, hasNext={}, nextCursor={}, nextAfter={}",
                pageBook.getSize(),
                pageBook.getNumberOfElements(),
                pageBook.hasNext(),
                nextCursor,
                nextAfter
        );
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
        BookGlobalApiDto globalApiDto = bookApiManager.searchWithFallback(isbn);
        return new NaverBookDto(globalApiDto.title(), globalApiDto.author(), globalApiDto.description(),
                globalApiDto.publisher(), globalApiDto.publishedDate(), globalApiDto.isbn(), globalApiDto.thumbnailImage());
    }

    @Override
    @Transactional(readOnly = true)
    public String extractIsbnFromImage(MultipartFile image) {
        return "";
    }

    @Override
    @Transactional
    public BookDto createBook(BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {
        if (bookRepository.existsByIsbn(bookCreateRequest.isbn())) {
            log.warn("책 생성 실패: 이미 존재하는 ISBN - {}", bookCreateRequest.isbn());
            throw new BookException(ErrorCode.DUPLICATE_BOOK_ISBN);
        }
        String fullS3Key = null;

        if (thumbnail != null && !thumbnail.isEmpty()) {
            fullS3Key = generateUniqueKey(thumbnail.getOriginalFilename());

            fileStorage.upload(thumbnail, fullS3Key);
            log.debug("썸네일 업로드 완료 - Key: {}", fullS3Key);
            bookCreateFailedRollbackCleanup(fullS3Key);
        }
        log.info("책 생성 요청 - 제목: {}, ISBN: {}",
                bookCreateRequest.title(), bookCreateRequest.isbn());
        Book savedBook = bookRepository.save(
                Book.create(
                        bookCreateRequest.title(),
                        bookCreateRequest.author(),
                        bookCreateRequest.isbn(),
                        bookCreateRequest.publishedDate(),
                        bookCreateRequest.publisher(),
                        fullS3Key,
                        bookCreateRequest.description()
                ));

        String finalCdnUrl = (fullS3Key != null) ? fileStorage.generateUrl(fullS3Key) : null;

        log.info("책 생성 완료 - ID: {}, 제목: {}", savedBook.getId(), savedBook.getTitle());

        return BookMapper.toDto(savedBook, finalCdnUrl, 0L, 0.0);
    }

    @Override
    @Transactional
    public BookDto updateBook(UUID bookId, BookUpdateRequest bookUpdateRequest, MultipartFile thumbnail) {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));
        String newKey = null;
        String oldKeyToDelete = null;

        if (existingBook.isDeleted()) {
            throw new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND);
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String newOriginalFileName = thumbnail.getOriginalFilename();
            newKey = generateUniqueKey(newOriginalFileName);

            fileStorage.upload(thumbnail, newKey);
            log.info("썸네일 업로드(수정) 완료 - Key: {}", newKey);

            bookCreateFailedRollbackCleanup(newKey);

            oldKeyToDelete = existingBook.getThumbnailUrl();
        }
        existingBook.update(
                bookUpdateRequest.title(),
                bookUpdateRequest.author(),
                bookUpdateRequest.publishedDate(),
                bookUpdateRequest.publisher(),
                bookUpdateRequest.description(),
                (newKey != null) ? newKey : existingBook.getThumbnailUrl()
        );

        if (oldKeyToDelete != null) {
            deleteFileAfterCommit(oldKeyToDelete);
        }
        String cdnUrl = fileStorage.generateUrl(existingBook.getThumbnailUrl());
        log.info("책 수정 완료 - ID: {}", existingBook.getId());

//        BookDto dto = getBookDetail(bookId); // TODO: 같은 서비스 메서드 연동해서 사용하면 의존성이 생기고 해당 서비스 메서드 로직 바뀔시 동작 안할수있기때문에 개별로 레포지토리 요청할것(update서비스에서 thumbnail 매핑하는데 단일 레포에서도 해야하기떄문에 이렇게 중복사용시 사이드이팩 생길여부 생김) 또한 사용자 요청을 위한 메서드호출이지 내부 로직을 위한 메서드가 아니라 응용하지말것, 테스트코드에서 이부분 update할떄 의존하는부분 문제로 캐치했었음, by 태언
        BookDto dto = bookRepository.findBookDetailById(bookId) // TODO: 성연님 부재중이라 기존 테스트코드 그대로 가져가기위해 임시로 수정, 성연님께 피드백후 해당부분 사용 여부 결정하시도록 전달 By 태언
                .orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));
        return BookMapper.toDto(existingBook, cdnUrl, dto.reviewCount(), dto.rating());
    }

    @Override
    @Transactional
    public void softDeleteBook(UUID bookId) {
        Book foundBook = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));
        foundBook.delete();
        log.debug("도서 논리 삭제 완료 - bookId={}", bookId);
    }

    @Override
    @Transactional
    public void hardDeleteBook(UUID bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND);
        }
        bookRepository.deleteById(bookId);
        log.debug("도서 물리 삭제 완료 - bookId={}", bookId);
    }

    private void bookCreateFailedRollbackCleanup(String fileKey) {
        if (fileKey == null) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        log.warn("트랜잭션 롤백 감지: S3 업로드 파일 삭제 시도 - Key: {}", fileKey);
                        fileStorage.delete(fileKey);
                    }
                }
            });

        } else {
            log.debug("트랜잭션 비활성 상태라 롤백 클린업 등록을 스킵합니다. (Unit Test 환경 예상)");
        }
    }

    private void deleteFileAfterCommit(String fileKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        try {
                            log.info("트랜잭션 커밋 성공: 구형 파일 삭제 시도 - Key: {}", fileKey);
                            fileStorage.delete(fileKey);
                        } catch (Exception e) {
                            log.error("구형 파일 삭제 실패 (고아 객체 발생, 추후 정리 필요) - Key: {}", fileKey, e);
                        }
                    }
                }
            });
        }
    }

    private String generateUniqueKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = getExtension(originalFilename);
        return "books/" + uuid + "." + ext;
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
