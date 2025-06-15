package com.example.newsforeveryone.notification.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;
import java.util.Map;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException(Map<String, Object> details) {
    super(ErrorCode.NOTIFICATION_NOT_FOUND, details);
  }

}
