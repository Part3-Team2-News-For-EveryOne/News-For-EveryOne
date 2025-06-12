package com.example.newsforeveryone.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
    @NotNull
    String articleId,

    @NotNull
    String userId,

    @NotBlank
    String content
) { }
