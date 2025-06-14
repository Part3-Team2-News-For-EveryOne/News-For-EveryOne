package com.example.newsforeveryone.notification.dto.response;

import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public record CursorPageNotificationResponse<T>(
    @JsonProperty("content")
    List<T> contents,

    @JsonProperty("nextCursor")
    String nextCursor,

    @JsonProperty("nextAfter")
    String nextAfter,

    @JsonProperty("size")
    Integer size,

    @JsonProperty("totalElements")
    Long totalElements,

    @JsonProperty("hasNext")
    boolean hasNext
) {

  public static CursorPageNotificationResponse<NotificationResult> FromNotification(
      Slice<Notification> notifications, Long totalElement) {

    String nextCursor = getNextCursor(notifications);
    List<NotificationResult> notificationResults = notifications.getContent()
        .stream()
        .map(NotificationResult::FromEntity)
        .toList();

    return new CursorPageNotificationResponse<>(
        notificationResults,
        nextCursor,
        null,
        notifications.getSize(),
        totalElement,
        notifications.hasNext()
    );
  }

  private static String getNextCursor(Slice<Notification> notifications) {
    if (notifications.getContent().isEmpty()) {
      return null;
    }

    return notifications.getContent().get(notifications.getContent().size() - 1).getCreatedAt()
        .toString();
  }

}
