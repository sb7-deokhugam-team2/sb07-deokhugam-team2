package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class BookException extends DeokhugamException {

    public BookException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BookException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public BookException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
