package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewNotFoundException extends ReviewException {

    public ReviewNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
