package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public abstract class CommentException extends BaseException {

  public CommentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}
