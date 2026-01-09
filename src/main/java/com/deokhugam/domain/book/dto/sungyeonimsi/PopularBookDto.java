package com.deokhugam.domain.book.dto.sungyeonimsi;

import com.deokhugam.domain.book.enums.Period;

import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
        UUID id,
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        Period period,
        Long rank,
        Double score,
        Long reviewCount,
        Double rating,
        Instant createdAt) {

}
