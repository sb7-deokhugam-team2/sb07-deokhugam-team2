package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewInvalidException extends ReviewException {

    public ReviewInvalidException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewInvalidException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewInvalidException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
