package com.deokhugam.domain.comment.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentReviewNullException extends CommentException {
    public CommentReviewNullException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentReviewNullException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentReviewNullException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
