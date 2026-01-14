package com.deokhugam.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeokhugamException.class)
    public ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e) {
        log.error("[DeokhugamException] = {}", e.getMessage());
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("[BindException] = {}", e.getMessage());
        Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid input",
                        (a, b) -> a, // 같은 필드 중복시 첫 값 유지
                        LinkedHashMap::new
                ));
        ErrorCode code = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        details,
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("[HttpMediaTypeNotSupportedException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("[MissingRequestHeaderException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.error("[IllegalStateException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.INVALID_STATE;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        log.error("[NoSuchElementException] = {}", e.getMessage());
        ErrorCode code = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        code.getCode(),
                        code.getMessage(),
                        Map.of("reason", e.getMessage()),
                        e.getClass().getSimpleName(),
                        code.getStatus().value()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Exception] = {}", e.getMessage());
        log.error("[Exception] = {}", e.getClass().getSimpleName());
        ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                code.getCode(),
                                e.getMessage(),
                                Map.of(),
                                e.getClass().getSimpleName(),
                                code.getStatus().value()
                        )
                );
    }
}
