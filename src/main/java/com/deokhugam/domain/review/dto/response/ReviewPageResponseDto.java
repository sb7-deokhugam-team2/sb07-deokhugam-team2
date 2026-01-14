package com.deokhugam.domain.review.dto.response;

import java.time.Instant;
import java.util.List;

public record ReviewPageResponseDto(
        List<ReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
