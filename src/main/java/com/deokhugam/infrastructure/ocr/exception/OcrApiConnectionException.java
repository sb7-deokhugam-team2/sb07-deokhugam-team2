package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OcrApiConnectionException extends OcrException{
    public OcrApiConnectionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OcrApiConnectionException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OcrApiConnectionException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
