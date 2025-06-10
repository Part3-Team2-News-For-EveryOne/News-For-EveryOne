package com.example.newsforeveryone.newsarticle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public record CursorPageArticleRequest(
    String keyword,
    Long interestId,
    List<String> sourceIn,
    Instant publishDateFrom,
    Instant publishDateTo,

    @NotNull
    String orderBy,

    @NotNull
    String direction,

    String cursor,
    Instant after,

    @NotNull
    @Min(value = 1)
    @Max(value = 100)
    Integer limit
) {
    public String getOrderByWithDefault() {
        return (this.orderBy == null) ? "publishdate" : this.orderBy.toLowerCase();
    }

    public String getDirectionWithDefault() {
        return (this.direction == null) ? "desc" : this.direction.toLowerCase();
    }
}
