package com.deokhugam.domain.book.controller;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<CursorPageResponseBookDto> getAllBooks(
            BookSearchCondition searchCondition
    ) {
        log.debug("도서 목록 조회 요청 - bookId={}, keyword={}, orderBy={}, direction={}, limit={}, cursor={}, after={}",
                searchCondition.keyword(),
                searchCondition.orderBy(),
                searchCondition.direction(),
                searchCondition.limit(),
                searchCondition.cursor(),
                searchCondition.after());
        CursorPageResponseBookDto cursorPageResponseBookDto = bookService.searchBooks(searchCondition);
        return ResponseEntity.ok(cursorPageResponseBookDto);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) {
        log.debug("도서 단일 조회 요청 - bookId={}", bookId);
        BookDto bookDetail = bookService.getBookDetail(bookId);
        return ResponseEntity.ok(bookDetail);
    }

    @GetMapping("/info")
    public ResponseEntity<NaverBookDto> getBookInfoByIsbn(@RequestParam String isbn) {
        return null;
    }

    @PostMapping()
    public ResponseEntity<BookDto> createBook(
            @RequestPart(value = "bookData") BookUpdateRequest updateRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        return null;
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable UUID bookId,
            @RequestPart(value = "bookData") BookUpdateRequest updateRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        return null;
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        log.debug("도서 논리 삭제 요청 - bookId={}", bookId);
        bookService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{bookId}")
    public ResponseEntity<Void> hardDeleteBook(@PathVariable UUID bookId) {
        log.debug("도서 물리 삭제 요청 - bookId={}", bookId);
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }


}
