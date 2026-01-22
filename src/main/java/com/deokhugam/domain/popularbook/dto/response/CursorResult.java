package com.deokhugam.domain.popularbook.dto.response;

import java.util.List;

public record CursorResult<T>(
        List<T> content,
        boolean hasNext,
        long total
) {
}
