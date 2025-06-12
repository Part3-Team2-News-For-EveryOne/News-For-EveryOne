package com.example.newsforeveryone.newsarticle.exception;

import static com.example.newsforeveryone.common.exception.ErrorCode.ARTICLE_NOT_FOUND;

import java.util.Map;

public class ArticleNotFoundException extends ArticleException {

  public ArticleNotFoundException(Map<String, Object> details) {
    super(ARTICLE_NOT_FOUND, details);
  }
}
