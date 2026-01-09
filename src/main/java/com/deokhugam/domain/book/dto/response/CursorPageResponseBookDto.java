package com.deokhugam.domain.book.dto.response;

import java.util.List;

public record CursorPageResponseBookDto(
        List<BookDto> content,
        String nextCursor,
        Long nextAfter,
        Integer size,
        Long totalElements,
        boolean hasNext
) {
}
