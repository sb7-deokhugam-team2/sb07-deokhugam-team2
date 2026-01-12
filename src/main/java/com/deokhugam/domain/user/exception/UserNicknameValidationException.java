package com.deokhugam.domain.user.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class UserNicknameValidationException extends UserException{
    public UserNicknameValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserNicknameValidationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserNicknameValidationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
