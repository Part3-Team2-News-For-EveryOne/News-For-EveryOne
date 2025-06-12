package com.example.newsforeveryone.useractivity.repository.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record CommentLikeActivityProjection(
    Long id,
    Instant createdAt,
    Long commentId,
    Long articleId,
    String articleTitle,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) {
  @QueryProjection
  public CommentLikeActivityProjection {
  }
}
