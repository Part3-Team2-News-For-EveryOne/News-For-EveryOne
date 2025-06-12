package com.example.newsforeveryone.comment.dto;

import java.time.Instant;

public record CommentLikeResponse(
    String id,
    String commentId,
    String articleId,
    String commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) { }
