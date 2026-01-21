package com.deokhugam.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "U-001"),
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
    COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "댓글에 대한 권한이 없습니다.", "C-005"),
    // Book
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도서를 찾을 수 없습니다.", "B-001"),
    BOOK_INVALID_SORT_CRITERIA(HttpStatus.BAD_REQUEST, "정렬기준 요청값이 잘못되었습니다.","B-002"),
    DUPLICATE_BOOK_ISBN(HttpStatus.BAD_REQUEST, "중복된 도서 ISBN입니다.","B-003"),
    BOOK_NO_EXISTENT_ISBN(HttpStatus.BAD_REQUEST, "존재하지않는 ISBN입니다.","B-004"),
    // Popular Book
    // Power User
    POWER_USER_NOT_SUPPORTED_EXCEPTION(HttpStatus.BAD_REQUEST, "지원하지 않는 타입입니다.", "PU-001"),
    // Review
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "리뷰가 이미 존재합니다.","R-001"),
    REVIEW_INVALID(HttpStatus.BAD_REQUEST,"유효하지 않는 요청 입니다.","R-002"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND,"리뷰를 찾을 수 없습니다.","R-003"),
    REVIEW_ID_NOT_EQUAL(HttpStatus.UNAUTHORIZED, "ID가 일치하지 않습니다.", "R-004"),
    REVIEW_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "cursor 형식이 올바르지 않습니다", "R-005"),
    REVIEW_AFTER_REQUIRED(HttpStatus.BAD_REQUEST, "RATING 정렬 시 after값은 필수입니다.", "R-006"),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "리뷰작성자만 수정할 수 있습니다.","R-007"),
    // Popular Review
    // Notification
    // LikedReview
    // common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,"요청이 올바르지 않습니다.", "CM-001"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서버 내부 오류가 발생했습니다.", "CM-002"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", "CM-003"),
    INVALID_STATE(HttpStatus.CONFLICT,"요청을 처리할 수 없는 상태입니다.", "CM-004"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"요청값이 올바르지 않습니다.", "CM-005"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"지원하지 않는 미디어 타입입니다.", "CM-006"),

    //api
    BOOK_NOT_FOUND_IN_API(HttpStatus.NOT_FOUND, "API에서 해당 도서를 찾을 수 없습니다.", "api-001"),

    // naver
    NAVER_API_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "네이버 API 연동 중 오류가 발생했습니다.", "NAVER-001"),
    NAVER_API_RESPONSE_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 API 응답 처리 중 오류가 발생했습니다.", "NAVER-002"),


    //Storage
    FAIL_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다.", "S3-001"),
    EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.", "S3-002"),
    IO_EXCEPTION_ON_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 입출력 오류가 발생했습니다.", "S3-003"),
    NO_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "파일 확장자가 없습니다.", "S3-004"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다.", "S3-005"),
    PUT_OBJECT_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "S3에 이미지를 저장하는 데 실패했습니다.", "S3-006"),
    FAIL_TO_GENERATE_URL(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 URL 생성에 실패했습니다.", "S3-007"),
    FAIL_TO_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.", "S3-008"),

    //ocr
    OCR_EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.", "OCR-001"),
    OCR_ISBN_EXTRACT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR 응답이 비어있거나 결과가 없습니다.", "OCR-002"),
    OCR_API_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR API 서버 내부 오류 발생", "OCR-003"),
    OCR_FILE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR 이미지 파일 처리 실패", "OCR-004"),
    OCR_API_CONNECTION_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "OCR API 연동 실패", "OCR-005")
    ;



    private final String message;
    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
