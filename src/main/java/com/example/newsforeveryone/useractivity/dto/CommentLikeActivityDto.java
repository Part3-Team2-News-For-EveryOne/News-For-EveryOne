package com.example.newsforeveryone.useractivity.dto;

import com.example.newsforeveryone.useractivity.repository.projection.CommentLikeActivityProjection;
import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record CommentLikeActivityDto(
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

  public static CommentLikeActivityDto from(CommentLikeActivityProjection p) {
    return new CommentLikeActivityDto(
        p.id(),
        p.createdAt(),
        p.commentId(),
        p.articleId(),
        p.articleTitle(),
        p.commentUserId(),
        p.commentUserNickname(),
        p.commentContent(),
        p.commentLikeCount(),
        p.commentCreatedAt()
    );
  }
}
