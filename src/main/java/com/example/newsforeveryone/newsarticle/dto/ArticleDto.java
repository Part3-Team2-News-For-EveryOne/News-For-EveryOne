package com.example.newsforeveryone.newsarticle.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record ArticleDto(
    Long id,
    String source,
    String sourceUrl,
    String title,
    Instant publishDate,
    String summary,
    Long commentCount,
    Long viewCount,
    Boolean viewedByMe
) {
  @QueryProjection
  public ArticleDto {}
}
