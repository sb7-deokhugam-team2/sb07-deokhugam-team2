package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRConnectionException extends OCRException {
    public OCRConnectionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRConnectionException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRConnectionException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
