package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRIsbnExtractFailedException extends OCRException {
    public OCRIsbnExtractFailedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRIsbnExtractFailedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRIsbnExtractFailedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
