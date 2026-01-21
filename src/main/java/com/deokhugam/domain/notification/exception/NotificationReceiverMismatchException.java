package com.deokhugam.domain.notification.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NotificationReceiverMismatchException extends NotificationException {
    public NotificationReceiverMismatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationReceiverMismatchException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationReceiverMismatchException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
