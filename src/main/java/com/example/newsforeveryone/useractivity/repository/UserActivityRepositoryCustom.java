package com.example.newsforeveryone.useractivity.repository;

import com.example.newsforeveryone.useractivity.repository.projection.ArticleViewActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentLikeActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.SubscriptionActivityProjection;
import java.util.List;
import org.springframework.stereotype.Repository;

public interface UserActivityRepositoryCustom {
  List<SubscriptionActivityProjection> findSubscriptionActivities(Long userId);
  List<CommentActivityProjection> findCommentActivities(Long userId, int limit);
  List<CommentLikeActivityProjection> findCommentLikeActivities(Long userId, int limit);
  List<ArticleViewActivityProjection> findArticleViewActivities(Long userId, int limit);
}
