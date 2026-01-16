package com.deokhugam.domain.book.exception;

import com.deokhugam.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class NoexistentISBN extends BookException{

    public NoexistentISBN(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoexistentISBN(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NoexistentISBN(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
