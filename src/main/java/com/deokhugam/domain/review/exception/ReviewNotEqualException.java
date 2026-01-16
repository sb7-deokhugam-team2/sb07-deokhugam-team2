package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewNotEqualException extends ReviewException{

    public ReviewNotEqualException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewNotEqualException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewNotEqualException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
