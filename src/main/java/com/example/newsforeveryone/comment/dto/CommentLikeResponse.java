package com.example.newsforeveryone.comment.dto;

import java.time.Instant;

public record CommentLikeResponse(
    Long id,
    Long commentId,
    Long articleId,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) { }
