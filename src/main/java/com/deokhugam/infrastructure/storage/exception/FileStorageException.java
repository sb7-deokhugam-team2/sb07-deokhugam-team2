package com.deokhugam.infrastructure.storage.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FileStorageException extends DeokhugamException {
    public FileStorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileStorageException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FileStorageException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
