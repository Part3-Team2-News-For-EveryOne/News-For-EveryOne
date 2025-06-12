package com.example.newsforeveryone.useractivity.repository.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record CommentActivityProjection(
    Long id,
    Long articleId,
    String articleTitle,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    Instant createdAt
) {

  @QueryProjection
  public CommentActivityProjection {
  }
}
