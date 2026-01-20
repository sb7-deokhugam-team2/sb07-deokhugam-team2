package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRFileProcessingException extends OCRException {
    public OCRFileProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRFileProcessingException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRFileProcessingException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
