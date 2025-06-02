package com.example.newsforeveryone.common.exception;

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
}
