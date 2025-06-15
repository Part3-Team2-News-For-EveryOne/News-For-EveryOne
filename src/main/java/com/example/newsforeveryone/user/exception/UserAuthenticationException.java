package com.example.newsforeveryone.user.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class UserAuthenticationException extends UserException {

  public UserAuthenticationException(Map<String, Object> details) {
    super(ErrorCode.INVALID_CREDENTIALS, details);
  }

}
