package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserAlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserAlreadyExistsException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
