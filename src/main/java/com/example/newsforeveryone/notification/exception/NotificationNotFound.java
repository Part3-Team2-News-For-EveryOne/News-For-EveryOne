package com.example.newsforeveryone.notification.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class NotificationNotFound extends NotificationException {

  public NotificationNotFound(Map<String, Object> details) {
    super(ErrorCode.NOTIFICATION_NOT_FOUND, details);
  }

}
