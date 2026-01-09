package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class UserNotFoundException extends UserException{
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
