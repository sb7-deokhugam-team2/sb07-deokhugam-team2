package com.deokhugam.domain.likedreview.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class LikedReviewException extends DeokhugamException {

    public LikedReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LikedReviewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public LikedReviewException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
