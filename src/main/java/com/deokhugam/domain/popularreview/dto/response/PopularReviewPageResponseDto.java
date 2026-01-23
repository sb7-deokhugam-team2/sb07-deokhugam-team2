package com.deokhugam.domain.popularreview.dto.response;

import java.time.Instant;
import java.util.List;

public record PopularReviewPageResponseDto(
        List<PopularReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
