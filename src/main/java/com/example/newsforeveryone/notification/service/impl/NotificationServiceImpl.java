package com.example.newsforeveryone.notification.service.impl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.exception.InterestNotFoundException;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;
import com.example.newsforeveryone.notification.entity.Notification;
import com.example.newsforeveryone.notification.exception.NotificationNotFound;
import com.example.newsforeveryone.notification.repository.NotificationRepository;
import com.example.newsforeveryone.notification.service.NotificationService;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.exception.UserNotFoundException;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final InterestRepository interestRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Override
  public NotificationResult createNotificationByInterest(long userId, long interestId,
      long count) {
    validateIsEnrolledUser(userId);
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));

    Notification notification = Notification.ofInterest(userId, interest.getId(),
        interest.getName(), count);
    Notification savedNotification = notificationRepository.save(notification);

    return NotificationResult.FromEntity(savedNotification);
  }

  @Override
  public NotificationResult createNotificationByComment(long authorId, long likerId,
      long commentId) {
    validateIsEnrolledUser(authorId);
    User liker = userRepository.findById(likerId)
        .orElseThrow(() -> new UserNotFoundException(Map.of("liker-id", likerId)));

    Notification notification = Notification.ofComment(authorId, commentId,
        liker.getNickname());
    Notification savedNotification = notificationRepository.save(notification);

    return NotificationResult.FromEntity(savedNotification);
  }

  @Transactional(readOnly = true)
  @Override
  public CursorPageNotificationResponse<NotificationResult> getAllIn(
      NotificationSearchRequest notificationSearchRequest, long userId) {
    validateIsEnrolledUser(userId);

    Instant cursor = parseCursor(notificationSearchRequest.cursor());
    PageRequest pageRequest = PageRequest.of(0, notificationSearchRequest.limit(),
        Sort.by("createdAt").descending());
    Page<Notification> notifications = notificationRepository.findAllByUserIdWithCursorAsc(userId,
        cursor, pageRequest);

    return CursorPageNotificationResponse.FromNotification(notifications);
  }

  @Transactional
  @Override
  public void confirmAllNotifications(long userId) {
    validateIsEnrolledUser(userId);

    List<Notification> notifications = notificationRepository.findAllByUserIdAndConfirmed(userId,
        true);
    for (Notification notification : notifications) {
      notification.confirmNotification();
    }

    notificationRepository.saveAll(notifications);
  }

  @Transactional
  @Override
  public void confirmNotification(long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFound(Map.of("notification-id", notificationId)));

    notification.confirmNotification();
    notificationRepository.save(notification);
  }

  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  @Override
  public void deleteConfirmedNotifications() {
    notificationRepository.deleteAllByConfirmedTrue();
  }

  private Instant parseCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return Instant.EPOCH;
    }

    try {
      return Instant.parse(cursor);
    } catch (DateTimeParseException e1) {
      return Instant.EPOCH;
    }
  }

  private void validateIsEnrolledUser(long userId) {
    if (userRepository.existsById(userId)) {
      return;
    }

    throw new UserNotFoundException(Map.of("user-id", userId));
  }

}
