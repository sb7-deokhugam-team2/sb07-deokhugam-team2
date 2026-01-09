package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class BookNotFoundException extends BookException{
    public BookNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public BookNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
