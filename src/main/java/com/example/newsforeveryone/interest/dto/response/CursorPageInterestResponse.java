package com.example.newsforeveryone.interest.dto.response;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CursorPageInterestResponse<T>(
    @JsonProperty("content")
    List<T> contents,

    @JsonProperty("nextCursor")
    String nextCursor,

    @JsonProperty("nextAfter")
    String nextAfter,

    @JsonProperty("size")
    Integer size,

    @JsonProperty("totalElements")
    Long totalElements,

    @JsonProperty("hasNext")
    boolean hasNext
) {

  public static CursorPageInterestResponse<InterestResult> fromEntity(
      List<InterestResult> interestResults,
      String nextCursor,
      String nextAfter,
      Long totalElements,
      boolean hasNext
  ) {
    return new CursorPageInterestResponse<>(
        interestResults,
        nextCursor,
        nextAfter,
        interestResults.size(),
        totalElements,
        hasNext
    );
  }

}
