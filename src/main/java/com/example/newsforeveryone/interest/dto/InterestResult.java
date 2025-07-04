package com.example.newsforeveryone.interest.dto;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InterestResult(
    @JsonProperty("id")
    long id,
    @JsonProperty("name")
    String interestName,
    @JsonProperty("keywords")
    List<String> keywords,
    @JsonProperty("subscriberCount")
    int subscriberCount,
    @JsonProperty("subscribedByMe")
    Boolean subscribedByMe
) {

  public static InterestResult fromEntity(
      Interest interest,
      List<Keyword> keywords,
      Boolean subscribedByMe
  ) {
    List<String> keywordNames = keywords.stream()
        .map(Keyword::getName)
        .toList();

    return new InterestResult(
        interest.getId(),
        interest.getName(),
        keywordNames,
        interest.getSubscriberCount(),
        subscribedByMe
    );
  }

}
