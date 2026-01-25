package com.deokhugam.domain.book.mapper;

import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookApiMapper {

    public static NaverBookDto toNaverBookDto(BookGlobalApiDto globalApiDto) {
        if (globalApiDto == null) {
            return null;
        }

        return new NaverBookDto(
                globalApiDto.title(),
                globalApiDto.author(),
                globalApiDto.description(),
                globalApiDto.publisher(),
                globalApiDto.publishedDate(),
                globalApiDto.isbn(),
                globalApiDto.thumbnailImage()
        );
    }
}