package com.deokhugam.domain.book.controller;

import com.deokhugam.domain.book.controller.docs.BookControllerDocs;
import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookControllerDocs {

    private final BookService bookService;

    @GetMapping
    @Override
    public ResponseEntity<CursorPageResponseBookDto> getAllBooks(
            BookSearchCondition searchCondition
    ) {
        log.debug("도서 목록 조회 요청 - keyword={}, orderBy={}, direction={}, limit={}, cursor={}, after={}",
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
    @Override
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) {
        log.debug("도서 단일 조회 요청 - bookId={}", bookId);
        BookDto bookDetail = bookService.getBookDetail(bookId);
        return ResponseEntity.ok(bookDetail);
    }

    @PostMapping("/isbn/ocr")
    @Override
    public ResponseEntity<String> getIsbnByImage(
            @RequestPart(value = "image") MultipartFile barcode) {
        log.info("ISBN OCR 요청 - FileName: {}, Size: {}", barcode.getOriginalFilename(), barcode.getSize());
        String isbn = bookService.extractIsbnFromImage(barcode);
        log.info("ISBN OCR 추출 성공 - ISBN: {}", isbn);
        return ResponseEntity.ok(isbn);
    }

    @GetMapping("/info")
    @Override
    public ResponseEntity<NaverBookDto> getBookInfoByIsbn(@RequestParam String isbn) {
        log.info("네이버 도서 정보 조회 요청 - ISBN: {}", isbn);
        NaverBookDto bookInfo = bookService.getBookByIsbn(isbn);
        log.debug("네이버 도서 정보 조회 완료 - Title: {}", bookInfo.title());
        return ResponseEntity.ok(bookInfo);
    }

    @PostMapping()
    @Override
    public ResponseEntity<BookDto> createBook(
            @Valid @RequestPart(value = "bookData") BookCreateRequest createRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        String fileName = (thumbnail != null) ? thumbnail.getOriginalFilename() : "NONE";

        String safeTitle = Encode.forJava(createRequest.title());
        String safeFileName = Encode.forJava(fileName);
        String safeIsbn = Encode.forJava(createRequest.isbn());

        log.info("도서 생성 요청 - Title: {}, ISBN: {}, Thumbnail: {}", safeTitle, safeIsbn, safeFileName);

        BookDto dto = bookService.createBook(createRequest, thumbnail);

        log.info("도서 생성 완료 - Generated ID: {}", dto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping("/{bookId}")
    @Override
    public ResponseEntity<BookDto> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestPart(value = "bookData") BookUpdateRequest updateRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    ) {
        String fileName = (thumbnail != null) ? thumbnail.getOriginalFilename() : "NONE";
        log.info("도서 수정 요청 - ID: {}, Title: {}, ThumbnailChange: {}", bookId, updateRequest.title(), fileName);

        BookDto dto = bookService.updateBook(bookId, updateRequest, thumbnail);

        log.info("도서 수정 완료 - ID: {}", dto.id());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{bookId}")
    @Override
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        log.debug("도서 논리 삭제 요청 - bookId={}", bookId);
        bookService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{bookId}")
    @Override
    public ResponseEntity<Void> hardDeleteBook(@PathVariable UUID bookId) {
        log.debug("도서 물리 삭제 요청 - bookId={}", bookId);
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }


}
