package com.deokhugam.domain.popularbook.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class PopularBookException extends DeokhugamException {
    public PopularBookException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PopularBookException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public PopularBookException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
