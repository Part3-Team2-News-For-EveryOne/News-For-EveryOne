package com.example.newsforeveryone.notification.service;

import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.notification.dto.NotificationResult;
import com.example.newsforeveryone.notification.dto.request.NotificationSearchRequest;
import com.example.newsforeveryone.notification.dto.response.CursorPageNotificationResponse;
import com.example.newsforeveryone.notification.entity.Notification;
import com.example.newsforeveryone.notification.entity.ResourceType;
import com.example.newsforeveryone.notification.repository.NotificationRepository;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private NotificationService notificationService;

    @Transactional
    @DisplayName("관심사 알림을 생성합니다.")
    @Test
    void createNotificationByInterest() {
        // given
        User savedUser = userRepository.save(new User("", "", ""));
        Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
        long count = 0;

        // when
        NotificationResult notificationByInterest = notificationService.createNotificationByInterest(savedUser, savedInterest, count);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(notificationByInterest.content()).isNotNull();
            softly.assertThat(notificationByInterest)
                    .extracting(NotificationResult::resourceType, NotificationResult::resourceId)
                    .containsExactlyInAnyOrder(ResourceType.INTEREST.name(), savedInterest.getId());
        });
    }

    @Disabled
    @Transactional
    @DisplayName("좋아요 알림을 생성합니다.")
    @Test
    void createNotificationByComment() {
        // given
        User author = userRepository.save(new User("user", "user", "user"));
        User liker = userRepository.save(new User("liker", "liker", "liker"));

        Comment comment = Comment.builder()
                .articleId(null)
                .userId(author.getId())
                .content("")
                .build();
        Comment savedComment = commentRepository.save(comment);

        // when
        NotificationResult notificationByComment = notificationService.createNotificationByComment(author, liker, savedComment);

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
        Notification firstNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
        Notification secondNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
        Notification thridNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
        firstNotification.confirmNotification();
        notificationRepository.save(firstNotification);
        NotificationSearchRequest notificationSearchRequest = new NotificationSearchRequest(null, null, 1);

        // when
        CursorPageNotificationResponse<NotificationResult> notifications = notificationService.getAllIn(notificationSearchRequest, savedUser.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(notifications.contents())
                    .extracting(NotificationResult::id, NotificationResult::resourceId, NotificationResult::userId, NotificationResult::resourceType, NotificationResult::confirmed, NotificationResult::content)
                    .containsExactlyElementsOf(
                            List.of(Tuple.tuple(secondNotification.getId(), secondNotification.getResourceId(), secondNotification.getUserId(), secondNotification.getResourceType().name(), secondNotification.getConfirmed(), secondNotification.getContent()))
                    );
            softly.assertThat(notifications)
                    .extracting(CursorPageNotificationResponse::hasNext, CursorPageNotificationResponse::nextCursor, CursorPageNotificationResponse::nextAfter, CursorPageNotificationResponse::size, CursorPageNotificationResponse::totalElements)
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
        Notification firstNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
        Notification secondNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));

        // when
        notificationService.confirmAllNotifications(savedUser.getId());

        // then
        Assertions.assertThat(notificationRepository.findAllByUserIdAndConfirmed(savedUser.getId(), false)).hasSize(2);
    }

    @Transactional
    @DisplayName("하나의 알림을 확인합니다.")
    @Test
    void confirmOneNotification() {
        // given
        User savedUser = userRepository.save(new User("", "", ""));
        Interest savedInterest = interestRepository.save(new Interest("러닝머신"));
        Notification firstNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));

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
        Notification firstNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
        Notification secondNotification = notificationRepository.save(Notification.ofInterest(savedUser.getId(), savedInterest.getId(), savedInterest.getName(), 0));
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

}