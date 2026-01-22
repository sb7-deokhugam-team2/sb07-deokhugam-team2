package com.deokhugam.domain.review.enums;

import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.global.exception.ErrorCode;

public enum ReviewOrderBy {
    CREATED_AT,
    RATING;

    public static ReviewOrderBy from(String raw) {
        if (raw == null) return CREATED_AT;
        return switch (raw) {
            case "createdAt" -> CREATED_AT;
            case "rating" -> RATING;
            default -> throw new ReviewInvalidException(ErrorCode.REVIEW_INVALID);
        };
    }
}
