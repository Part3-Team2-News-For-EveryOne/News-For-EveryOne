package com.example.newsforeveryone.interest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InterestRegisterRequest(
        @NotBlank
        @JsonProperty("name")
        String name,

        @NotEmpty
        @JsonProperty("keywords")
        List<String> keywords
) {
}
