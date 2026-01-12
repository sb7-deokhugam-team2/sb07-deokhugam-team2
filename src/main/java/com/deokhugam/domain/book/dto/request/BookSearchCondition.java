package com.deokhugam.domain.book.dto.request;

import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;

public record BookSearchCondition(
        String keyword,
        SortCriteria orderBy,
        SortDirection direction,
        String cursor,
        String after,
        Integer limit
) {
    public BookSearchCondition {
        if (orderBy == null) orderBy = SortCriteria.TITLE;
        if (direction == null) direction = SortDirection.DESC;
        if (limit == null || limit <= 0) limit = 50;
    }
}
