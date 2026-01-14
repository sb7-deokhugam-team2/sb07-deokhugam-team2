package com.deokhugam.domain.comment.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentUnauthorizedException extends CommentException{
    public CommentUnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentUnauthorizedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentUnauthorizedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
