package com.deokhugam.domain.book.dto.request;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.enums.SortDirection;

public record PopularBookSearchCondition(
        PeriodType period,
        SortDirection sortDirection,

        String cursor,
        String afterCursor,

        Integer limit
) {
    public PopularBookSearchCondition{
        if(period == null) period = PeriodType.DAILY;
        if(sortDirection == null) sortDirection = SortDirection.ASC;
        if(limit <= 0) limit = 50;
    }
}
