package com.deokhugam.domain.book.mapper;

import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;

public class BookMapper {
    public static BookDto bookToBookDto(Book book, String presignedUrl) {
        return new BookDto(book.getId(),
                book.getTitle(), book.getAuthor(),
                book.getDescription(), book.getPublisher(),
                book.getPublishedDate(), book.getIsbn(),
                presignedUrl, 0, 0.0,
                book.getCreatedAt(), book.getUpdatedAt());
    }
    public static BookDto bookToBookDto(Book book, String presignedUrl, Long reviewCount, Double rating) {
        return new BookDto(book.getId(),
                book.getTitle(), book.getAuthor(),
                book.getDescription(), book.getPublisher(),
                book.getPublishedDate(), book.getIsbn(),
                presignedUrl, reviewCount, rating,
                book.getCreatedAt(), book.getUpdatedAt());
    }
}
