package com.deokhugam.domain.book.mapper;

import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookMapper {
    public static BookDto toDto(Book book, Long reviewCount, Double rating) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getIsbn(),
                book.getThumbnailUrl(), // 엔티티의 Key를 그대로 전달
                reviewCount,
                rating,
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
