package com.deokhugam.domain.popularbook.mapper;

import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.book.mapper.BookThumbnailUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PopularBookUrlMapper {
    private final BookThumbnailUrlResolver bookThumbnailUrlResolver;

    public PopularBookDto withFullThumbnailUrl(PopularBookDto popularBookDto) {
        String fullUrl = bookThumbnailUrlResolver.toFullUrl(popularBookDto.thumbnailUrl());
        return new PopularBookDto(
                popularBookDto.id(),
                popularBookDto.bookId(),
                popularBookDto.title(),
                popularBookDto.author(),
                fullUrl,
                popularBookDto.period(),
                popularBookDto.rank(),
                popularBookDto.score(),
                popularBookDto.reviewCount(),
                popularBookDto.rating(),
                popularBookDto.createdAt()
        );
    }

    public List<PopularBookDto> withFullThumbnailUrl(List<PopularBookDto> popularBookDtos) {
        return popularBookDtos.stream().map(this::withFullThumbnailUrl).toList();
    }

}
