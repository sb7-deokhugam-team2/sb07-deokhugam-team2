package com.deokhugam.domain.popularbook.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularBookDto(
        List<PopularBookDto> content,
        String nextCursor,
        Instant nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
