package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewException extends DeokhugamException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
