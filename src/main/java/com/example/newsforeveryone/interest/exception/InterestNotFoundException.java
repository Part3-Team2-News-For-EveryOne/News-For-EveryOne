package com.example.newsforeveryone.interest.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class InterestNotFoundException extends InterestException {

  public InterestNotFoundException(Map<String, Object> details) {
    super(ErrorCode.INTEREST_NOT_FOUND, details);
  }

}