package com.deokhugam.domain.book.enums;

import com.deokhugam.domain.book.exception.BookInvalidSortCriteriaException;
import com.deokhugam.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SortCriteria {
    TITLE("title"),
    PUBLISHED_DATE("publishedDate"),
    RATING("rating"),
    REVIEW_COUNT("reviewCount");

    private final String criteria;

    public static SortCriteria from(String value) {
        return Arrays.stream(values())
                .filter(sortCriteria -> sortCriteria.criteria.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BookInvalidSortCriteriaException(ErrorCode.BOOK_INVALID_SORT_CRITERIA, Map.of("sortValue", value)));
    }
}
