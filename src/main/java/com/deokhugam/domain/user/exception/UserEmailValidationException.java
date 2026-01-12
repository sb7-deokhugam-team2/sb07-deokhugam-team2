package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserEmailValidationException extends UserException{
    public UserEmailValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserEmailValidationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserEmailValidationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
