package com.deokhugam.infrastructure.search.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ApiConnectionException extends ApiException {
    public ApiConnectionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ApiConnectionException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ApiConnectionException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
