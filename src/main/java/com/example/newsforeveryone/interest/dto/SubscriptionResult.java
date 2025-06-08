package com.example.newsforeveryone.interest.dto;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record SubscriptionResult(
        @JsonProperty("id")
        SubscriptionId id,
        @JsonProperty("interestId")
        Long interestId,
        @JsonProperty("interestName")
        String interestName,
        @JsonProperty("interestKeyword")
        List<String> interestKeyword,
        @JsonProperty("interestSubscriberCount")
        Integer interestSubscriberCount,
        @JsonProperty("createdAt")
        Instant createdAt
) {

    public static SubscriptionResult fromEntity(Subscription subscription, List<String> keywords) {
        Interest interest = subscription.getInterest();
        return new SubscriptionResult(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                keywords,
                interest.getSubscriberCount(),
                interest.getCreatedAt()
        );
    }

}
