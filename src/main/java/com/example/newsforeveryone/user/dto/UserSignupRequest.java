package com.example.newsforeveryone.user.dto;

public record UserSignupRequest(
    String email,
    String nickname,
    String password
) { }
