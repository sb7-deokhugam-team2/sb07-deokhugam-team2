package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserPasswordException extends UserException {
    public UserPasswordException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserPasswordException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserPasswordException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
