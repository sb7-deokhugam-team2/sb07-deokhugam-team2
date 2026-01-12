package com.deokhugam.domain.comment.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentUserNullException extends CommentException {
    public CommentUserNullException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentUserNullException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentUserNullException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
