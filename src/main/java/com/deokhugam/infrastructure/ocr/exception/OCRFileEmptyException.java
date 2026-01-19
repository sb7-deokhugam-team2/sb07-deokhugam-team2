package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRFileEmptyException extends OCRException {
    public OCRFileEmptyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRFileEmptyException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRFileEmptyException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
