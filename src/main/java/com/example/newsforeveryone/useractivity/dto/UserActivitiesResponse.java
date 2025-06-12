package com.example.newsforeveryone.useractivity.dto;

import java.time.Instant;
import java.util.List;

public record UserActivitiesResponse(
    Long id,
    String email,
    String nickname,
    Instant createdAt,
    List<SubscriptionActivityDto> subscriptions,
    List<CommentActivityDto> comments,
    List<CommentLikeActivityDto> commentLikes,
    List<ArticleViewActivityDto> articleViews
) {

}
