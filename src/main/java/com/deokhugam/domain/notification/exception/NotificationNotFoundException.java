package com.deokhugam.domain.notification.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NotificationNotFoundException extends NotificationException {
    public NotificationNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
