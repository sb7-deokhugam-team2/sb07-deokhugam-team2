package com.deokhugam.domain.review.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class ReviewAlreadyExistsException extends ReviewException {

    public ReviewAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewAlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewAlreadyExistsException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
