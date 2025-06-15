package com.example.newsforeveryone.useractivity.service;

import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.exception.UserNotFoundException;
import com.example.newsforeveryone.user.repository.UserRepository;
import com.example.newsforeveryone.useractivity.dto.ArticleViewActivityDto;
import com.example.newsforeveryone.useractivity.dto.CommentActivityDto;
import com.example.newsforeveryone.useractivity.dto.CommentLikeActivityDto;
import com.example.newsforeveryone.useractivity.dto.SubscriptionActivityDto;
import com.example.newsforeveryone.useractivity.dto.UserActivitiesResponse;
import com.example.newsforeveryone.useractivity.repository.UserActivityRepositoryCustom;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

  private final UserRepository userRepository;
  private final UserActivityRepositoryCustom userActivityRepository;

  private static final int ACTIVITY_LIMIT = 10;

  @Override
  public UserActivitiesResponse getUserActivities(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));

    log.info("Fetching activities for user: {}", userId);

    List<SubscriptionActivityDto> subscriptions = userActivityRepository.findSubscriptionActivities(
            userId)
        .stream()
        .map(SubscriptionActivityDto::from)
        .toList();

    List<CommentActivityDto> comments = userActivityRepository.findCommentActivities(userId,
            ACTIVITY_LIMIT)
        .stream()
        .map(CommentActivityDto::from)
        .toList();

    List<CommentLikeActivityDto> likes = userActivityRepository.findCommentLikeActivities(userId,
            ACTIVITY_LIMIT)
        .stream()
        .map(CommentLikeActivityDto::from)
        .toList();

    List<ArticleViewActivityDto> views = userActivityRepository.findArticleViewActivities(userId,
            ACTIVITY_LIMIT)
        .stream()
        .map(ArticleViewActivityDto::from)
        .toList();

    return new UserActivitiesResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt(),
        subscriptions,
        comments,
        likes,
        views
    );
  }
}
