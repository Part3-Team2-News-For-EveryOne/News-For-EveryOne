package com.example.newsforeveryone.notification.dto;

import com.example.newsforeveryone.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record NotificationResult(
        @JsonProperty("id")
        Long id,
        @JsonProperty("createdAt")
        Instant createdAt,
        @JsonProperty("updateAt")
        Instant updatedAt,
        @JsonProperty("confirmed")
        Boolean confirmed,
        @JsonProperty("userId")
        Long userId,
        @JsonProperty("content")
        String content,
        @JsonProperty("resourceType")
        String resourceType,
        @JsonProperty("resourceId")
        Long resourceId
) {
    public static NotificationResult FromEntity(Notification notification) {
        return new NotificationResult(
                notification.getId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getConfirmed(),
                notification.getUserId(),
                notification.getContent(),
                notification.getResourceType().name(),
                notification.getResourceId()
        );
    }

}
