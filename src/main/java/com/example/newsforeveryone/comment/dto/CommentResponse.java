package com.example.newsforeveryone.comment.dto;

import java.time.Instant;

public record CommentResponse(
    Long id,
    Long articleId,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    Boolean likedByMe,
    Instant createdAt
) { }
