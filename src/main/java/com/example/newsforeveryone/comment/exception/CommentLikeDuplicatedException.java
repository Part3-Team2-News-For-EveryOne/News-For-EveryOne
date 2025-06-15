package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentLikeDuplicatedException extends CommentException {

  public CommentLikeDuplicatedException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_LIKE_DUPLICATED, details);
  }

}
