package com.deokhugam.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청이 올바르지 않습니다.", "U-001"),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이메일 중복", "U-002"),
    USER_EMAIL_NOT_EXISTS(HttpStatus.UNAUTHORIZED, "이메일이 존재하지 않습니다.", "U-003"),
    USER_PASSWORD_NOT_EQUAL(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.", "U-004"),
    USER_EMAIL_VALIDATION(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다", "U-005"),
    USER_PASSWORD_VALIDATION(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.", "U-006"),
    USER_NICKNAME_VALIDATION(HttpStatus.BAD_REQUEST, "닉네임의 형식이 올바르지 않습니다.", "U-007"),
    // Comment
    COMMENT_CONTENT_VALIDATION(HttpStatus.BAD_REQUEST, "댓글 형식이 올바르지 않습니다.", "C-001"),
    COMMENT_USER_NULL(HttpStatus.BAD_REQUEST, "유저는 Null일 수 없습니다.", "C-002"),
    COMMENT_REVIEW_NULL(HttpStatus.BAD_REQUEST, "리뷰는 Null일 수 없습니다.", "C-003"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.", "C-004"),
    COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.", "C-005"),
    // Book
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도서를 찾을 수 없습니다.", "B-001"),
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
