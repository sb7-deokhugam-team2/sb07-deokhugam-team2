package com.deokhugam.domain.book.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BookService {
    BookDto getBookDetail(UUID bookId);

    CursorPageResponseBookDto searchBooks(BookSearchCondition bookSearchCondition);

    NaverBookDto getBookByIsbn(String isbn);

    String extractIsbnFromImage(MultipartFile image);

    BookDto createBook(BookCreateRequest bookCreateRequest, MultipartFile thumbnail);

    BookDto updateBook(UUID bookId, BookUpdateRequest bookUpdateRequest, MultipartFile thumbnail);

    void softDeleteBook(UUID bookId);

    void hardDeleteBook(UUID bookId);


}
