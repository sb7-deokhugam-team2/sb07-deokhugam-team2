package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OrcFileProcessingException extends OcrException{
    public OrcFileProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrcFileProcessingException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OrcFileProcessingException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
