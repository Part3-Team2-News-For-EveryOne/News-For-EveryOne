package com.example.newsforeveryone.common.exception.external;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class RateLimitExceededException extends ExternalApiException {
  public RateLimitExceededException(Map<String,Object> details) {
    super(ErrorCode.EXTERNAL_API_RATE_LIMIT, details);
  }
}