package com.example.newsforeveryone.newsarticle.exception;

import static com.example.newsforeveryone.common.exception.ErrorCode.ARTICLE_METRIC_NOT_FOUND;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class ArticleMetricNotFoundException extends ArticleException {

  public ArticleMetricNotFoundException(Map<String, Object> details) {
    super(ErrorCode.ARTICLE_METRIC_NOT_FOUND, details);
  }

}
