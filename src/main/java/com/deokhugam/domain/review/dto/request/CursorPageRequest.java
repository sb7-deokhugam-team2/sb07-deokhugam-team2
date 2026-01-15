package com.deokhugam.domain.review.dto.request;

import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;

public record CursorPageRequest(
        ReviewOrderBy orderBy,
        SortDirection direction,
        String cursor,
        Instant after,

        @Min(1)
        @Max(50)
        Integer limit
) {
    public static final int DEFAULT_LIMIT = 50;

    public CursorPageRequest {
        if (orderBy == null) orderBy = ReviewOrderBy.CREATED_AT;
        if (direction == null) direction = SortDirection.DESC;
        if (limit == null || limit <= 0) limit = DEFAULT_LIMIT;
    }
}
