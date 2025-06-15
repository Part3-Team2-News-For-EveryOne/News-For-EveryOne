package com.example.newsforeveryone.newsarticle.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class ArticleNotFoundException extends ArticleException {

  public ArticleNotFoundException(Map<String, Object> details) {
    super(ErrorCode.ARTICLE_NOT_FOUND, details);
  }

}
