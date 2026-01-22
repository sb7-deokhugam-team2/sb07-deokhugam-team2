package com.deokhugam.domain.popularbook.dto.request;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.enums.SortDirection;

import java.time.Instant;

public record PopularBookSearchCondition(
        PeriodType period,
        SortDirection direction,

        String cursor,
        Instant after,

        Integer limit
) {
    public PopularBookSearchCondition{
        if(period == null) period = PeriodType.DAILY;
        if(direction == null) direction = SortDirection.ASC;
        if(limit <= 0) limit = 50;
    }
}
