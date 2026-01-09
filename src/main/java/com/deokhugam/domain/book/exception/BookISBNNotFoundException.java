package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class BookISBNNotFoundException extends BookException{
    public BookISBNNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookISBNNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public BookISBNNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
