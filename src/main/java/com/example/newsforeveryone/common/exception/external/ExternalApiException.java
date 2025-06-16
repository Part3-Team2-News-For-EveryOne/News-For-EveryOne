package com.example.newsforeveryone.common.exception.external;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class ExternalApiException extends BaseException {
  public ExternalApiException(ErrorCode errorCode, Map<String,Object> details) {
    super(errorCode, details);
  }
}
