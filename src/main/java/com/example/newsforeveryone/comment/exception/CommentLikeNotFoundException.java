package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentLikeNotFoundException extends CommentException {

  public CommentLikeNotFoundException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_LIKE_NOT_FOUND, details);
  }

}
