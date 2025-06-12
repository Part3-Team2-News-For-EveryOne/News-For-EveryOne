package com.example.newsforeveryone.notification.service;

import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;
import com.example.newsforeveryone.user.entity.User;

public interface NotificationService {

  NotificationResult createNotificationByInterest(User user, Interest interest, long count);

  NotificationResult createNotificationByComment(User author, User liker, Comment comment);

  CursorPageNotificationResponse<NotificationResult> getAllIn(
      NotificationSearchRequest notificationSearchRequest, long userId);

  void confirmAllNotifications(long userId);

  void confirmNotification(long notificationId);

  void deleteConfirmedNotifications();

}
