package com.example.newsforeveryone.notification.service;

import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;

public interface NotificationService {

  NotificationResult createNotificationByInterest(long userId, long interestId, long count);

  NotificationResult createNotificationByComment(long authorId, long likerId, long commentId);

  CursorPageNotificationResponse<NotificationResult> getAllIn(
      NotificationSearchRequest notificationSearchRequest, long userId);

  void confirmAllNotifications(long userId);

  void confirmNotification(long notificationId);

  void deleteConfirmedNotifications();

}
