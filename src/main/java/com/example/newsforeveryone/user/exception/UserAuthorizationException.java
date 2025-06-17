package com.example.newsforeveryone.user.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class UserAuthorizationException extends UserException {

  public UserAuthorizationException(Map<String, Object> details) {
    super(ErrorCode.UNAUTHORIZED_USER_ACCESS, details);
  }

}
