package com.example.newsforeveryone.useractivity.repository.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;
import java.util.List;

public record SubscriptionActivityProjection(
    Long id,
    Long interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    Instant createdAt
) {

  @QueryProjection
  public SubscriptionActivityProjection {
  }
}
