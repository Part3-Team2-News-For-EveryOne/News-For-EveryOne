package com.example.newsforeveryone.user.dto;

public record UserLoginRequest(
    String email,
    String password
) { }
