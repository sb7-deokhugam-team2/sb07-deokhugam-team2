package com.deokhugam.global.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class DeokhugamException extends RuntimeException{
    private final Instant timestamp;
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public DeokhugamException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public DeokhugamException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public DeokhugamException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);

    }
}
