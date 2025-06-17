package com.example.newsforeveryone.newsarticle.exception;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public abstract class ArticleException extends BaseException {

  public ArticleException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}
