package com.example.newsforeveryone.useractivity.dto;

import com.example.newsforeveryone.useractivity.repository.projection.SubscriptionActivityProjection;
import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;
import java.util.List;

public record SubscriptionActivityDto(
    Long id,
    Long interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    Instant createdAt
) {

  public static SubscriptionActivityDto from(SubscriptionActivityProjection p) {
    return new SubscriptionActivityDto(
        p.id(),
        p.interestId(),
        p.interestName(),
        p.interestKeywords(),
        p.interestSubscriberCount(),
        p.createdAt()
    );
  }
}
