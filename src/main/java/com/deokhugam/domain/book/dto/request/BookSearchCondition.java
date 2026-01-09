package com.deokhugam.domain.book.dto.request;

import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;

public record BookSearchCondition(
        String keyword,

        SortCriteria sortCriteria,
        SortDirection sortDirection,

        String cursor,
        String afterCursor,

        Integer limit
) {
    public BookSearchCondition{
        if(sortCriteria == null) sortCriteria = SortCriteria.TITLE;
        if(sortDirection == null) sortDirection = SortDirection.DESC;
        if(limit <= 0) limit = 50;
    }
}
