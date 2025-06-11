package com.example.newsforeveryone.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
    @NotNull
    Long articleId,

    @NotNull
    Long userId,

    @NotBlank
    String content
) { }
