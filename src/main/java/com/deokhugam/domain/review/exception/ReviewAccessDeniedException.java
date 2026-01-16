package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewAccessDeniedException extends ReviewException {

    public ReviewAccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewAccessDeniedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewAccessDeniedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
