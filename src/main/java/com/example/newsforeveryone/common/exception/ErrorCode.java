package com.example.newsforeveryone.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
  INVALID_CREDENTIALS(401, "U01", "이메일 또는 비밀번호가 올바르지 않습니다"),
  UNAUTHORIZED_USER_ACCESS(403, "U02", "해당 사용자에 대한 권한이 없습니다"),
  USER_NOT_FOUND(404, "U03", "사용자를 찾을 수 없습니다"),
  DUPLICATE_EMAIL(409, "U04", "이미 존재하는 이메일입니다"),

  ARTICLE_NOT_FOUND(404,"A03", "기사를 찾을 수 없습니다"),

  INVALID_REQUEST(400, "C01", "잘못된 요청입니다"),
  INTERNAL_SERVER_ERROR(500, "C02", "서버 내부 오류가 발생했습니다");

  private final int httpStatus;
  private final String code;
  private final String message;

}
