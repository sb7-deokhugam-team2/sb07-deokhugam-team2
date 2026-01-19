package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OcrIsbnExtractFailedException extends OcrException {
    public OcrIsbnExtractFailedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OcrIsbnExtractFailedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OcrIsbnExtractFailedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
