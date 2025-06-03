package com.example.newsforeveryone.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank
    String nickname
) { }
