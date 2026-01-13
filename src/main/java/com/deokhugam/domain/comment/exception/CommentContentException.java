package com.deokhugam.domain.comment.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentContentException extends CommentException {
    public CommentContentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentContentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentContentException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
