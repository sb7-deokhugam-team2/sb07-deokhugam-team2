package com.deokhugam.domain.poweruser.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class PowerUserNotSupportedPeriodTypeException extends PowerUserException{
    public PowerUserNotSupportedPeriodTypeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PowerUserNotSupportedPeriodTypeException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public PowerUserNotSupportedPeriodTypeException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
