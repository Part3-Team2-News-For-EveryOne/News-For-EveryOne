package com.example.newsforeveryone.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
  INVALID_REQUEST(400, "C01", "잘못된 요청입니다"),
  INVALID_CREDENTIALS(401, "U03", "이메일 또는 비밀번호가 올바르지 않습니다"),
  UNAUTHORIZED_USER_ACCESS(403, "U04", "해당 사용자에 대한 권한이 없습니다"),
  DUPLICATE_EMAIL(409, "U01", "이미 존재하는 이메일입니다"),
  INTERNAL_SERVER_ERROR(500, "C02", "서버 내부 오류가 발생했습니다");

  private final int httpStatus;
  private final String code;
  private final String message;
}
