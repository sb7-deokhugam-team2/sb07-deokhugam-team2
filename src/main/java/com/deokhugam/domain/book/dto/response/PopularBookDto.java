package com.deokhugam.domain.book.dto.response;

import com.deokhugam.domain.base.PeriodType;
import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
        UUID id,
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        PeriodType period,
        Long rank,
        Double score,
        Long reviewCount,
        Double rating,
        Instant createdAt
) {

}
