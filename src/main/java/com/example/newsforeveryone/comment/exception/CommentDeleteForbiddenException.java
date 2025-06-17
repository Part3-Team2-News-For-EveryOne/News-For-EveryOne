package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentDeleteForbiddenException extends CommentException {

  public CommentDeleteForbiddenException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_DELETE_FORBIDDEN, details);
  }

}
