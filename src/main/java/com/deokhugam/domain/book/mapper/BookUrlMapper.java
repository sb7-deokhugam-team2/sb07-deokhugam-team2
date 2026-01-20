package com.deokhugam.domain.book.mapper;

import com.deokhugam.domain.book.dto.response.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookUrlMapper {
    private final BookThumbnailUrlResolver thumbnailUrlResolver;

    public BookDto withFullThumbnailUrl(BookDto bookDto) {
        String fullUrl = thumbnailUrlResolver.toFullUrl(bookDto.thumbnailUrl());
        return new BookDto(
                bookDto.id(),
                bookDto.title(),
                bookDto.author(),
                bookDto.description(),
                bookDto.publisher(),
                bookDto.publishedDate(),
                bookDto.isbn(),
                fullUrl,
                bookDto.reviewCount(),
                bookDto.rating(),
                bookDto.createdAt(),
                bookDto.updatedAt()
        );
    }

    public List<BookDto> withFullThumbnailUrl(List<BookDto> list) {
        return list.stream().map(this::withFullThumbnailUrl).toList();
    }
}



