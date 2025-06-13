package com.example.newsforeveryone.notification.controller;

import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;
import com.example.newsforeveryone.notification.service.NotificationService;
import com.example.newsforeveryone.user.config.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorPageNotificationResponse<NotificationResult>> getAllNotification(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") Integer limit,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    CursorPageNotificationResponse<NotificationResult> notifications = notificationService.getAllIn(
        new NotificationSearchRequest(cursor, after, limit), userDetails.getUserId()
    );

    return ResponseEntity.ok(notifications);
  }

  @PatchMapping
  public ResponseEntity<Void> confirmAllNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    notificationService.confirmAllNotifications(userDetails.getUserId());

    return ResponseEntity.noContent()
        .build();
  }

  @PatchMapping("{notificationId}")
  public ResponseEntity<Void> confirmNotifications(
      @PathVariable("notificationId") Long notificationId
  ) {
    notificationService.confirmNotification(notificationId);

    return ResponseEntity.noContent()
        .build();
  }

}
