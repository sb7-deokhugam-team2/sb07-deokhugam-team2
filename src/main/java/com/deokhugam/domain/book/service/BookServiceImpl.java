package com.deokhugam.domain.book.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

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
        return null;
    }

    @Override
    @Transactional
    public BookDto updateBook(UUID bookId, BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {
        return null;
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
}
