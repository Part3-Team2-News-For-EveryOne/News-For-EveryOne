package com.example.newsforeveryone.interest.exception;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public abstract class InterestException extends BaseException {

  public InterestException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}
