package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OcrException extends DeokhugamException {
    public OcrException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OcrException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OcrException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
