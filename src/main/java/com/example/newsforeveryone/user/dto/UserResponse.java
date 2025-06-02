package com.example.newsforeveryone.user.dto;

public record UserResponse(
    String id,
    String email,
    String nickname,
    String createdAt
) { }
