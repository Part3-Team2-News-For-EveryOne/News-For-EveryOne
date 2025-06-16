package com.example.newsforeveryone.newsarticle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record CursorPageArticleRequest(
    String keyword,
    Long interestId,
    List<String> sourceIn,
    LocalDateTime publishDateFrom,
    LocalDateTime publishDateTo,

    @NotNull
    String orderBy,

    @NotNull
    String direction,

    String cursor,
    Instant after,

    @Min(value = 1)
    @Max(value = 100)
    Integer limit
) {
    public CursorPageArticleRequest {
        if (limit == null || limit == 0) {
            limit = 20;
        }
    }

    public String getOrderByWithDefault() {
        return (this.orderBy == null) ? "publishdate" : this.orderBy.toLowerCase();
    }

    public String getDirectionWithDefault() {
        return (this.direction == null) ? "desc" : this.direction.toLowerCase();
    }
}
