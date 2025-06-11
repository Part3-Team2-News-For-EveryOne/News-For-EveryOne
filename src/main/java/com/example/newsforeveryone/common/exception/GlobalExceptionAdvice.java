package com.example.newsforeveryone.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(
                                exception.getTimestamp(),
                                exception.getErrorCode(),
                                exception.getMessage(),
                                exception.getClass().getSimpleName(),
                                exception.getDetails()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception exception) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(
                                Instant.now(),
                                ErrorCode.INTERNAL_SERVER_ERROR,
                                exception.getMessage(),
                                exception.getClass().getSimpleName(),
                                Map.of()
                        )
                );
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, Object> details = new HashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            details.put(fieldName.substring(fieldName.lastIndexOf('.') + 1), violation.getMessage());
        });

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.of(
                Instant.now(),
                errorCode,
                "입력값 유효성 검증에 실패했습니다.",
                exception.getClass().getSimpleName(),
                details
            ));
    }
}
