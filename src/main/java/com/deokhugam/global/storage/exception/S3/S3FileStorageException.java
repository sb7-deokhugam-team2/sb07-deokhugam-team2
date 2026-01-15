package com.deokhugam.global.storage.exception.S3;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.storage.exception.FileStorageException;

import java.time.Instant;
import java.util.Map;

public class S3FileStorageException extends FileStorageException {
    public S3FileStorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public S3FileStorageException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public S3FileStorageException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
