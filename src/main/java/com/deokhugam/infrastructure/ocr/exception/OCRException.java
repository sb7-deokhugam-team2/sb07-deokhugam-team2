package com.deokhugam.infrastructure.ocr.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class OCRException extends DeokhugamException {
    public OCRException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OCRException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public OCRException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
