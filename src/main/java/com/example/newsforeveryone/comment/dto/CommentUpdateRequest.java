package com.example.newsforeveryone.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank
    String content
) { }
