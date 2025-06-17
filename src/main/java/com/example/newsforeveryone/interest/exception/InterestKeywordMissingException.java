package com.example.newsforeveryone.interest.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class InterestKeywordMissingException extends InterestException {

  public InterestKeywordMissingException(Map<String, Object> details) {
    super(ErrorCode.INTEREST_KEYWORD_MISSING, details);
  }

}
