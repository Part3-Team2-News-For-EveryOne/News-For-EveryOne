package com.example.newsforeveryone.user.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class UserDuplicateEmailException extends UserException {

  public UserDuplicateEmailException(Map<String, Object> details) {
    super(ErrorCode.DUPLICATE_EMAIL, details);
  }

}
