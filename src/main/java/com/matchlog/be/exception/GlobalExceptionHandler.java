package com.matchlog.be.exception;

import com.matchlog.be.dto.common.ErrorResponse;
import com.matchlog.be.exception.constant.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException: {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST_BODY.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST_BODY.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("TypeMismatch: {}", e.getMessage());
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST_BODY.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
