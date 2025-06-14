package com.example.newsforeveryone.notification.service.impl;

import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationFactoryService notificationFactoryService;
  private final SubscriptionRepository subscriptionRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional
  @Override
  public List<NotificationResult> createNotificationByInterest(
      List<ArticleInterestId> articleInterestIds) {
    if (articleInterestIds == null) {
      return List.of();
    }

    Map<Long, List<Long>> interestToArticle = mappingInterestToArticle(
        new HashSet<>(articleInterestIds));
    Set<Subscription> subscriptions = subscriptionRepository.findAllWithInterestByInterestIdIn(
        interestToArticle.keySet());
    List<Notification> notifications = notificationFactoryService.createNotifications(
        interestToArticle, subscriptions);

    List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

    return NotificationResult.FromEntity(savedNotifications);
  }

  private Map<Long, List<Long>> mappingInterestToArticle(
      Set<ArticleInterestId> articleInterestIds) {

    Map<Long, List<Long>> interestIdToArticles = new HashMap<>();
    for (ArticleInterestId articleInterestId : articleInterestIds) {
      List<Long> articleIds = interestIdToArticles.getOrDefault(articleInterestId.getInterestId(),
          new ArrayList<>());
      articleIds.add(articleInterestId.getArticleId());
      interestIdToArticles.put(articleInterestId.getInterestId(), articleIds);
    }

    return interestIdToArticles;
  }

  @Transactional
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
    PageRequest pageRequest = PageRequest.of(0, notificationSearchRequest.limit());
    Slice<Notification> notifications = notificationRepository.findAllByUserIdWithCursorAsc(userId,
        cursor, pageRequest);

    return CursorPageNotificationResponse.FromNotification(notifications,
        notificationRepository.countByUserIdAndConfirmed(userId, false));
  }

  @Transactional
  @Override
  public void confirmAllNotifications(long userId) {
    validateIsEnrolledUser(userId);

    List<Notification> notifications = notificationRepository.findAllByUserIdAndConfirmed(userId,
        false);
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
