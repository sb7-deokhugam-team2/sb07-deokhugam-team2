package com.deokhugam.domain.book.dto.request;

import com.deokhugam.domain.book.enums.Period;
import com.deokhugam.domain.book.enums.SortDirection;

public record PopularBookSearchCondition(
        Period period,
        SortDirection sortDirection,

        String cursor,
        String afterCursor,

        Integer limit
) {
    public PopularBookSearchCondition{
        if(period == null) period = Period.DAILY;
        if(sortDirection == null) sortDirection = SortDirection.ASC;
        if(limit <= 0) limit = 50;
    }
}
