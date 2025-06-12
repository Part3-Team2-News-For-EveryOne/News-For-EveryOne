package com.example.newsforeveryone.notification.service;

import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterest;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.ArticleInterestRepository;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;
import com.example.newsforeveryone.notification.entity.Notification;
import com.example.newsforeveryone.notification.entity.ResourceType;
import com.example.newsforeveryone.notification.repository.NotificationRepository;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class NotificationServiceTest extends IntegrationTestSupport {

  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private NewsArticleRepository newsArticleRepository;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private ArticleInterestRepository articleInterestRepository;
  @Autowired
  private NotificationService notificationService;

  @Transactional
  @DisplayName("구독한 관심사에 관련된 뉴스가 등록되면, 알림을 생성합니다.")
  @Test
  void createNotificationByInterest() {
    // given
    User firstUser = userRepository.save(new User("1", "1", "1"));
    Interest firstInterest = interestRepository.save(new Interest("축구"));
    Interest secondInterest = interestRepository.save(new Interest("농구"));
    subscriptionRepository.save(new Subscription(firstInterest, firstUser.getId()));
    subscriptionRepository.save(new Subscription(secondInterest, firstUser.getId()));
    NewsArticle firstNewsArticle = saveNewsArticle("1");
    ArticleInterest articleInterest = getArticleInterest(firstNewsArticle, firstInterest.getId());
    ArticleInterest secondArticleInterest = getArticleInterest(firstNewsArticle,
        secondInterest.getId());
    List<ArticleInterestId> articleInterestIds = List.of(articleInterest.getId(),
        secondArticleInterest.getId());

    // when
    List<NotificationResult> notificationByInterest = notificationService
        .createNotificationByInterest(articleInterestIds);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(notificationByInterest).hasSize(2);
      softly.assertThat(notificationByInterest)
          .extracting(NotificationResult::resourceType,
              NotificationResult::resourceId,
              NotificationResult::userId)
          .containsExactlyInAnyOrder(
              Tuple.tuple(ResourceType.INTEREST.name(), firstInterest.getId(), firstUser.getId()),
              Tuple.tuple(ResourceType.INTEREST.name(), secondInterest.getId(), firstUser.getId())
          );
    });
  }

  @Transactional
  @DisplayName("구독한 관심사에 관련된 뉴스가 등록되지않으면, 빈리스트를 반환합니다.")
  @Test
  void createNotificationByInterest_Null() {
    // when
    List<NotificationResult> notificationByInterest = notificationService
        .createNotificationByInterest(null);

    // then
    Assertions.assertThat(notificationByInterest).isEmpty();
  }


  @Transactional
  @DisplayName("좋아요 알림을 생성합니다.")
  @Test
  void createNotificationByComment() {
    // given
    User author = userRepository.save(new User("user", "user", "user"));
    User liker = userRepository.save(new User("liker", "liker", "liker"));
    NewsArticle savedNewsArticle = saveNewsArticle("");
    Comment savedComment = saveComment(author, savedNewsArticle);

    // when
    NotificationResult notificationByComment = notificationService.createNotificationByComment(
        author.getId(), liker.getId(), savedComment.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(notificationByComment.content()).isNotNull();
      softly.assertThat(notificationByComment)
          .extracting(NotificationResult::resourceType, NotificationResult::resourceId)
          .containsExactlyInAnyOrder(ResourceType.COMMENT.name(), savedComment.getId());
    });
  }

  @Transactional
  @DisplayName("알림목록을 조회하면, 생성된 시간 순으로 오름차순 정렬해서 반환한다.")
  @Test
  void getAllIn() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
    Notification firstNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    Notification secondNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    Notification thridNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    firstNotification.confirmNotification();
    notificationRepository.save(firstNotification);
    NotificationSearchRequest notificationSearchRequest = new NotificationSearchRequest(null, null,
        1);

    // when
    CursorPageNotificationResponse<NotificationResult> notifications = notificationService.getAllIn(
        notificationSearchRequest, savedUser.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(notifications.contents())
          .extracting(NotificationResult::id, NotificationResult::resourceId,
              NotificationResult::userId, NotificationResult::resourceType,
              NotificationResult::confirmed, NotificationResult::content)
          .containsExactlyElementsOf(
              List.of(Tuple.tuple(secondNotification.getId(), secondNotification.getResourceId(),
                  secondNotification.getUserId(), secondNotification.getResourceType().name(),
                  secondNotification.getConfirmed(), secondNotification.getContent()))
          );
      softly.assertThat(notifications)
          .extracting(CursorPageNotificationResponse::hasNext,
              CursorPageNotificationResponse::nextCursor, CursorPageNotificationResponse::nextAfter,
              CursorPageNotificationResponse::size, CursorPageNotificationResponse::totalElements)
          .containsExactlyInAnyOrder(
              true, secondNotification.getCreatedAt().toString(), null, 1, 2L
          );
    });
  }

  @Transactional
  @DisplayName("모든 알림을 확인합니다.")
  @Test
  void confirmAllNotifications() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
    Notification firstNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    Notification secondNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));

    // when
    notificationService.confirmAllNotifications(savedUser.getId());

    // then
    Assertions.assertThat(
        notificationRepository.findAllByUserIdAndConfirmed(savedUser.getId(), false)).hasSize(2);
  }

  @Transactional
  @DisplayName("하나의 알림을 확인합니다.")
  @Test
  void confirmOneNotification() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
    Notification firstNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));

    // when
    notificationService.confirmNotification(firstNotification.getId());

    // then
    Assertions.assertThat(notificationRepository.findById(firstNotification.getId()))
        .isPresent()
        .get()
        .extracting(Notification::getConfirmed)
        .isEqualTo(true);
  }

  @Transactional
  @DisplayName("확인된 메서지들을 삭제합니다.")
  @Test
  void deleteBeforeDateConfirmedNotification() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
    Notification firstNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    Notification secondNotification = notificationRepository.save(
        Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(),
            0));
    firstNotification.confirmNotification();
    notificationRepository.save(firstNotification);

    // when
    notificationService.deleteConfirmedNotifications();

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(notificationRepository.findAll()).hasSize(1);
      softly.assertThat(notificationRepository.findAll())
          .extracting(Notification::getId)
          .isEqualTo(List.of(secondNotification.getId()));
    });
  }

  @NotNull
  private ArticleInterest getArticleInterest(NewsArticle newsArticle, Long interestId) {
    return articleInterestRepository.save(new ArticleInterest(
        new ArticleInterestId(newsArticle.getId(), interestId)));
  }

  @NotNull
  private Comment saveComment(User author, NewsArticle savedNewsArticle) {
    Comment comment = Comment.builder()
        .articleId(savedNewsArticle.getId())
        .userId(author.getId())
        .content("")
        .build();
    return commentRepository.save(comment);
  }

  @NotNull
  private NewsArticle saveNewsArticle(String link) {
    NewsArticle newsArticle = NewsArticle.builder()
        .interestIds(null)
        .title("")
        .summary("")
        .link(link)
        .sourceName("")
        .publishedAt(Instant.now())
        .build();

    return newsArticleRepository.save(newsArticle);
  }

}