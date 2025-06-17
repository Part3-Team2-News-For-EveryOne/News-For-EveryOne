package com.example.newsforeveryone.newsarticle.repository.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record ArticleProjection(
    Long id,
    String source,
    String sourceUrl,
    String title,
    Instant publishDate,
    String summary,
    Long commentCount,
    Long viewCount,
    Boolean viewedByMe,
    Instant createdAt
) {
  @QueryProjection
  public ArticleProjection {}
}