package com.deokhugam.domain.book.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
        List<BookDto> content,
        String nextCursor,
        Instant nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
