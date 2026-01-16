package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OcrFileEmptyException extends OcrException {
    public OcrFileEmptyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OcrFileEmptyException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OcrFileEmptyException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
