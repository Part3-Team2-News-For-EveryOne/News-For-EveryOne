package com.example.newsforeveryone.common.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

  private final Instant timestamp;
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public BaseException(ErrorCode errorCode) {
    this(errorCode, Map.of());
  }

  public BaseException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.getMessage());
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details;
  }

}
