package com.deokhugam.domain.comment.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentNotFound extends CommentException{
    public CommentNotFound(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentNotFound(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentNotFound(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
