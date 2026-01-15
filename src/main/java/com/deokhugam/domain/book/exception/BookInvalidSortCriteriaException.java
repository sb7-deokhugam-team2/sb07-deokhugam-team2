package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class BookInvalidSortCriteriaException extends BookException {

    public BookInvalidSortCriteriaException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookInvalidSortCriteriaException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public BookInvalidSortCriteriaException(Instant timeStamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timeStamp, errorCode, details);
    }
}
