package com.deokhugam.domain.popularbook.dto.response;

import java.util.UUID;

public record PopularBookAggregationDto(
        UUID bookId,
        Long reviewCount,
        Double avgRating,
        Double score
) {
}
