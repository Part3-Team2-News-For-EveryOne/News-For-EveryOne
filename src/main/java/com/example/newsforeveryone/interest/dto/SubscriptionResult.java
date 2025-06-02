package com.example.newsforeveryone.interest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record SubscriptionResult(
        @JsonProperty("id")
        long id,
        @JsonProperty("interestId")
        long interestId,
        @JsonProperty("interestName")
        String interestName,
        @JsonProperty("interestKeyword")
        List<String> interestKeyword,
        @JsonProperty("interestSubscriberCount")
        Integer interestSubscriberCount,
        @JsonProperty("createdAt")
        Instant createdAt
) {
}
