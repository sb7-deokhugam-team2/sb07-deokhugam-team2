package com.deokhugam.infrastructure.search.book.exception;

import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class BookNotFoundInApi extends BookException {
    public BookNotFoundInApi(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookNotFoundInApi(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public BookNotFoundInApi(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
