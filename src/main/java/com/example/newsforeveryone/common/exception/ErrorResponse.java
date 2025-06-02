package com.example.newsforeveryone.common.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;

@Builder
public record ErrorResponse(
    Instant timestamp, int status, String code, String message, String exceptionType, Map<String, Object> details
) {

  public static ErrorResponse of(Instant timestamp, ErrorCode errorCode, String message,
      String exceptionType, Map<String, Object> details) {
    return ErrorResponse.builder()
        .timestamp(timestamp)
        .code(errorCode.getCode())
        .message(message)
        .exceptionType(exceptionType)
        .status(errorCode.getHttpStatus())
        .details(details)
        .build();
  }
}



