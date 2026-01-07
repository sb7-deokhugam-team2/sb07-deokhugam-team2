package com.deokhugam.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User
    // Comment
    // Book
    // Popular Book
    // Power User
    // Review
    // Popular Review
    // Notification
    // LikedReview

    // common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,"요청이 올바르지 않습니다.", "CM-001"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서버 내부 오류가 발생했습니다.", "CM-002"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", "CM-003"),
    INVALID_STATE(HttpStatus.CONFLICT,"요청을 처리할 수 없는 상태입니다.", "CM-004");

    private final String message;
    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
