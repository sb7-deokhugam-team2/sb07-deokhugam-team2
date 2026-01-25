package com.deokhugam.domain.popularreview.dto.request;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.review.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;

public record PopularReviewSearchCondition(
        PeriodType period,
        SortDirection direction,
        String cursor,
        Instant after,

        @Min(1)
        @Max(50)
        Integer limit
) {
}
