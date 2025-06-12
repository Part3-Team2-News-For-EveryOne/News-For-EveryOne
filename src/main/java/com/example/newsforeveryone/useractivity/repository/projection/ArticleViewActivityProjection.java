package com.example.newsforeveryone.useractivity.repository.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record ArticleViewActivityProjection(
    Long id,
    Long viewedBy,
    Instant createdAt,
    Long articleId,
    String source,
    String sourceUrl,
    String articleTitle,
    Instant articlePublishedDate,
    String articleSummary,
    Long articleCommentCount,
    Long articleViewCount
) {

  @QueryProjection
  public ArticleViewActivityProjection {
  }
}
