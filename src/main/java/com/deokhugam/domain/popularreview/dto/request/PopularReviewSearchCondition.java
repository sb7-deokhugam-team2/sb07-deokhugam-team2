package com.deokhugam.domain.popularreview.dto.request;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.review.enums.SortDirection;

import java.time.Instant;

public record PopularReviewSearchCondition(
        PeriodType period,
        SortDirection direction,
        String cursor,
        Instant after,
        Integer limit
) {
}
