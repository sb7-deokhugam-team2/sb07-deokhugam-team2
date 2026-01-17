package com.deokhugam.domain.popularbook.dto.response;

import com.deokhugam.domain.popularbook.entity.PopularBook;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularBookDto(
        List<PopularBook> content,
        String nextCursor,
        Instant nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
