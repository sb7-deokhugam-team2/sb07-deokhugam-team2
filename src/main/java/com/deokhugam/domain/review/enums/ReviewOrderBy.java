package com.deokhugam.domain.review.enums;

public enum ReviewOrderBy {
    CREATED_AT,
    RATING;

    public static ReviewOrderBy from(String raw) {
        if (raw == null) return CREATED_AT;
        return switch (raw) {
            case "createdAt" -> CREATED_AT;
            case "rating" -> RATING;
            // TODO: Custom Exception 적용 예정
            default -> throw new IllegalArgumentException("Invalid orderBy: " + raw);
        };
    }
}
