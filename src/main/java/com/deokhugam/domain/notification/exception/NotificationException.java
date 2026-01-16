package com.deokhugam.domain.notification.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NotificationException extends DeokhugamException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
