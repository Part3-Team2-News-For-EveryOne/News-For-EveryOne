package com.example.newsforeveryone.interest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InterestResult(
        @JsonProperty("id")
        long id,
        @JsonProperty("name")
        String name,
        @JsonProperty("keywords")
        List<String> keywords,
        @JsonProperty("subscriberCount")
        Integer subscriberCount,
        @JsonProperty("subscribedByMe")
        boolean subscribedByMe
) {
}
