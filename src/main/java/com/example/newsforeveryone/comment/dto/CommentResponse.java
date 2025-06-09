package com.example.newsforeveryone.comment.dto;

import java.time.Instant;

public record CommentResponse(
    String id,
    String articleId,
    String userId,
    String userNickname,
    String content,
    Long likeCount,
    Boolean likedByMe,
    Instant createdAt
) { }
