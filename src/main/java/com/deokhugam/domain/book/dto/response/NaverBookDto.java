package com.deokhugam.domain.book.dto.response;

import java.time.LocalDate;

public record NaverBookDto(
        String title,
        String author,
        String description,
        String publisher,
        String publishedDate,
        String isbn,
        byte[] thumbnailImage
) {
}
