package com.deokhugam.global.storage;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.storage.exception.FileStorageException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public abstract class BaseFileStorage implements FileStorage{

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "text/plain"
    );

    protected void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new FileStorageException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }
}
