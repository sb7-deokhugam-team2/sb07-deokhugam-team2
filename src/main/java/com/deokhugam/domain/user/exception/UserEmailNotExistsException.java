package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserEmailNotExistsException extends UserException {
    public UserEmailNotExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserEmailNotExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserEmailNotExistsException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
