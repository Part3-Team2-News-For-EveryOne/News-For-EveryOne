package com.example.newsforeveryone.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSignupRequest(
    @NotBlank
    @Email
    String email,

    @NotBlank
    String nickname,

    @NotBlank
    String password
) { }
