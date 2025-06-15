package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentNotFoundException extends BaseException {

  public CommentNotFoundException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_NOT_FOUND, details);
  }

}
