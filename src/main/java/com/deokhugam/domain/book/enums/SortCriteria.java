package com.deokhugam.domain.book.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortCriteria {
    TITLE("title"),
    PUBLISHED_DATE("publishedDate"),
    RATING("rating"),
    REVIEW_COUNT("reviewCount");

    private final String criteria;
}
