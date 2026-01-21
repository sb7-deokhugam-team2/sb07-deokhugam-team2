package com.deokhugam.domain.poweruser.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class PowerUserException extends DeokhugamException {
    public PowerUserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PowerUserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public PowerUserException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
