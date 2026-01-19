package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRInternalException extends OCRException {
    public OCRInternalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRInternalException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRInternalException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
