package com.deokhugam.domain.book.controller;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<CursorPageResponseBookDto> getAllBooks(
            BookSearchCondition searchCondition
    ) {
        CursorPageResponseBookDto cursorPageResponseBookDto = bookService.searchBooks(searchCondition);
        return ResponseEntity.ok(cursorPageResponseBookDto);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) {
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
        bookService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{bookId}")
    public ResponseEntity<Void> hardDeleteBook(@PathVariable UUID bookId) {
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }


}
