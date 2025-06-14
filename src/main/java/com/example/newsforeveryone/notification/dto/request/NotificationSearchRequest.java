package com.example.newsforeveryone.notification.dto.request;

public record NotificationSearchRequest(
    String cursor,
    String after,
    Integer limit
) {

}
