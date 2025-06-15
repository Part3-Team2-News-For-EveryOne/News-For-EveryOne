package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentUpdateForbiddenException extends CommentException {

  public CommentUpdateForbiddenException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_UPDATE_FORBIDDEN, details);
  }

}
