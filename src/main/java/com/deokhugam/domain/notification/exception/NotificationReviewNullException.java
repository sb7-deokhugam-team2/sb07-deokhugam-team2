package com.deokhugam.domain.notification.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NotificationReviewNullException extends NotificationException{
    public NotificationReviewNullException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationReviewNullException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationReviewNullException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
