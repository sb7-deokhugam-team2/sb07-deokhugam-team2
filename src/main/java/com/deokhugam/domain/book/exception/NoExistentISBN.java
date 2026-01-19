package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NoExistentISBN extends BookException{

    public NoExistentISBN(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoExistentISBN(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NoExistentISBN(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
