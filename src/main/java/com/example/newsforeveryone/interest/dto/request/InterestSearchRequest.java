package com.example.newsforeveryone.interest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterestSearchRequest(
        @JsonProperty("keyword")
        String keyword,

        @NotBlank
        @JsonProperty("orderBy")
        String orderBy,

        @NotBlank
        @JsonProperty("direction")
        String direction,

        @JsonProperty("cursor")
        String cursor,

        @JsonProperty("after")
        String after,

        @NotNull
        @JsonProperty("limit")
        Integer limit
) {
}
