package com.example.newsforeveryone.comment.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class CommentAlreadyDeletedException extends CommentException {

  public CommentAlreadyDeletedException(Map<String, Object> details) {
    super(ErrorCode.COMMENT_ALREADY_DELETED, details);
  }

}
