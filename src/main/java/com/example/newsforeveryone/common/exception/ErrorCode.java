package com.example.newsforeveryone.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
  USER_NOT_FOUND(404, "U00","User does not exist."); // 예시

  private final int httpStatus;
  private final String code;
  private final String message;

}
