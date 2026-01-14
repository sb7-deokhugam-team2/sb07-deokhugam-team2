package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserPasswordValidationException extends UserException{
    public UserPasswordValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserPasswordValidationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserPasswordValidationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
