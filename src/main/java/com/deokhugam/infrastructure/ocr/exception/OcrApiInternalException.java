package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OcrApiInternalException extends OcrException{
    public OcrApiInternalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OcrApiInternalException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OcrApiInternalException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
