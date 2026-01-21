package com.deokhugam.domain.notification.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NotificationUserNullException extends NotificationException{
    public NotificationUserNullException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationUserNullException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationUserNullException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
