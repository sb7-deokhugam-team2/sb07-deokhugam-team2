package com.deokhugam.infrastructure.search.book.dto;

import com.deokhugam.infrastructure.search.book.enums.ProviderType;

import java.time.LocalDate;

public record BookGlobalApiDto(
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate,
        String isbn,
        byte[] thumbnailImage,
        ProviderType source
) {
}
