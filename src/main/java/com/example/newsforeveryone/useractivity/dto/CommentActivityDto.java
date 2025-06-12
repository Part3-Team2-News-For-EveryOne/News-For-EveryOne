package com.example.newsforeveryone.useractivity.dto;

import com.example.newsforeveryone.useractivity.repository.projection.CommentActivityProjection;
import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record CommentActivityDto(
    Long id,
    Long articleId,
    String articleTitle,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    Instant createdAt
) {

  public static CommentActivityDto from(CommentActivityProjection p) {
    return new CommentActivityDto(
        p.id(),
        p.articleId(),
        p.articleTitle(),
        p.userId(),
        p.userNickname(),
        p.content(),
        p.likeCount(),
        p.createdAt()
    );
  }
}
