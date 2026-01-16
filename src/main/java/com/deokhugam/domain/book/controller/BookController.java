package com.deokhugam.domain.book.controller;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
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
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @PostMapping()
    public ResponseEntity<BookDto> createBook(
            @RequestPart(value = "bookData") BookCreateRequest createRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        BookDto dto = bookService.createBook(createRequest, thumbnail);
        return ResponseEntity.status(201).body(dto);
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable UUID bookId,
            @RequestPart(value = "bookData") BookUpdateRequest updateRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        BookDto dto = bookService.updateBook(bookId, updateRequest, thumbnail);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        bookService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{bookId}")
    public ResponseEntity<Void> physicalDeleteBook(@PathVariable UUID bookId) {
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }


}
