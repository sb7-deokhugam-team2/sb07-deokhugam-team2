package com.deokhugam.infrastructure.search.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ApiException extends DeokhugamException {
    public ApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ApiException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ApiException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
