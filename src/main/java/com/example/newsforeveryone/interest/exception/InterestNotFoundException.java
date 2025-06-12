package com.example.newsforeveryone.interest.exception;

import static com.example.newsforeveryone.common.exception.ErrorCode.INTEREST_NOT_FOUND;

import java.util.Map;

public class InterestNotFoundException extends InterestException {

  public InterestNotFoundException(Map<String, Object> details) {
    super(INTEREST_NOT_FOUND, details);
  }

}
