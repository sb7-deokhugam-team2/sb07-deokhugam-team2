package com.deokhugam.domain.book.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.mapper.BookMapper;
import com.deokhugam.domain.book.mapper.BookUrlMapper;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.infrastructure.ocr.OCRApiClient;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.search.book.BookApiManager;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final FileStorage fileStorage;
    private final BookApiManager bookApiManager;
    private final OCRApiClient ocrApiClient;
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
        CursorResult<BookDto> pageBook = bookRepository.findBooks(bookSearchCondition, pageable);
        List<BookDto> content = bookUrlMapper.withFullThumbnailUrl(pageBook.content());
        BookDto last = content.isEmpty() ? null : content.get(content.size() - 1);
        String nextCursor = null;
        Instant nextAfter = null;
        if (pageBook.hasNext() && last != null) {
            nextCursor = buildNextCursor(bookSearchCondition, last);
            nextAfter = last.createdAt(); // 레포의 tie-breaker와 맞춤
        }
        log.debug("도서 목록 조회 완료 - limit={}, resultCount={}, hasNext={}, nextCursor={}, nextAfter={}",
                pageBook.content().size(),
                pageBook.total(),
                pageBook.hasNext(),
                nextCursor,
                nextAfter
        );
        return new CursorPageResponseBookDto(
                content,
                nextCursor,
                nextAfter,
                pageBook.content().size(),
                pageBook.total(),
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
    public NaverBookDto getBookByIsbn(String isbn) {
        BookGlobalApiDto globalApiDto = bookApiManager.searchWithFallback(isbn);
        return new NaverBookDto(globalApiDto.title(), globalApiDto.author(), globalApiDto.description(),
                globalApiDto.publisher(), globalApiDto.publishedDate(), globalApiDto.isbn(), globalApiDto.thumbnailImage());
    }

    @Override
    public String extractIsbnFromImage(MultipartFile image) {
        return ocrApiClient.extractIsbn(image);
    }

    @Override
    @Transactional
    public BookDto createBook(BookCreateRequest request, MultipartFile thumbnail) {
        Optional<Book> existingBookOp = bookRepository.findByIsbn(request.isbn());

        return existingBookOp.map(book -> restoreDeletedBook(book, request, thumbnail)).orElseGet(() -> createNewBook(request, thumbnail));
    }

    private BookDto restoreDeletedBook(Book targetBook, BookCreateRequest request, MultipartFile thumbnail) {
        if (!targetBook.isDeleted()) {
            log.warn("책 생성 실패: 이미 존재하는 ISBN - {}", request.isbn());
            throw new BookException(ErrorCode.DUPLICATE_BOOK_ISBN);
        }
        log.info("삭제된 도서 복구 및 업데이트 진행: {}", request.isbn());

        String newS3Key = targetBook.getThumbnailUrl();
        String oldKeyToDelete = null;

        if (isValidFile(thumbnail)) {
            newS3Key = uploadImageAndRegisterRollback(thumbnail);
            oldKeyToDelete = targetBook.getThumbnailUrl();
        }

        targetBook.restore();
        targetBook.update(
                request.title(), request.author(), request.publishedDate(),
                request.publisher(), request.description(), newS3Key
        );

        if (oldKeyToDelete != null && !oldKeyToDelete.equals(newS3Key)) {
            deleteFileAfterCommit(oldKeyToDelete);
        }

        return convertToDto(targetBook, newS3Key);
    }
    private BookDto createNewBook(BookCreateRequest request, MultipartFile thumbnail) {
        String fullS3Key = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            fullS3Key = generateUniqueKey(thumbnail.getOriginalFilename());
            fileStorage.upload(thumbnail, fullS3Key);
            bookCreateFailedRollbackCleanup(fullS3Key);
        }

        log.info("책 생성 요청 - 제목: {}, ISBN: {}", request.title(), request.isbn());
        Book savedBook = bookRepository.save(Book.create(
                request.title(),
                request.author(),
                request.isbn(),
                request.publishedDate(),
                request.publisher(),
                fullS3Key,
                request.description()
        ));
        log.info("책 생성 완료 - ID: {}", savedBook.getId());
        BookMapper.toDto(savedBook, 0L, 0.0);
        BookDto bookDto = BookMapper.toDto(savedBook, 0L, 0.0);
        return bookUrlMapper.withFullThumbnailUrl(bookDto);
    }

    @Override
    @Transactional
    public BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnail) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));

        if (existingBook.isDeleted()) throw new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND);

        String newKey = existingBook.getThumbnailUrl();
        String oldKeyToDelete = null;

        if (isValidFile(thumbnail)) {
            newKey = uploadImageAndRegisterRollback(thumbnail);
            oldKeyToDelete = existingBook.getThumbnailUrl();
        }

        existingBook.update(
                request.title(), request.author(), request.publishedDate(),
                request.publisher(), request.description(), newKey
        );

        if (oldKeyToDelete != null && !oldKeyToDelete.equals(newKey)) {
            deleteFileAfterCommit(oldKeyToDelete);
        }

        return convertToDto(existingBook, newKey);
    }

    private String uploadImageAndRegisterRollback(MultipartFile file) {
        String key = generateUniqueKey(file.getOriginalFilename());
        fileStorage.upload(file, key);
        log.debug("썸네일 업로드 완료 - Key: {}", key);

        bookCreateFailedRollbackCleanup(key);
        return key;
    }

    private BookDto convertToDto(Book book, String s3Key) {
        String cdnUrl = getCdnUrl(s3Key);
        BookDto dto = bookRepository.findBookDetailById(book.getId())
                .orElse(null);

        long reviewCount = (dto != null) ? dto.reviewCount() : 0L;
        double rating = (dto != null) ? dto.rating() : 0.0;

        return BookMapper.toDto(book, reviewCount, rating);
    }

    private String getCdnUrl(String key) {
        return (key != null) ? fileStorage.generateUrl(key) : null;
    }

    private boolean isValidFile(MultipartFile file) {
        return file != null && !file.isEmpty();
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
