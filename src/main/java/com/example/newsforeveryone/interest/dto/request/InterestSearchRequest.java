package com.example.newsforeveryone.interest.dto.request;

public record InterestSearchRequest(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    String after,
    Integer limit
) {

}
