package com.deokhugam.infrastructure.storage;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.storage.exception.FileStorageException;

import java.util.List;

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
