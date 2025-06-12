package com.example.newsforeveryone.useractivity.dto;

import com.example.newsforeveryone.useractivity.repository.projection.ArticleViewActivityProjection;
import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;

public record ArticleViewActivityDto(
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

  public static ArticleViewActivityDto from(ArticleViewActivityProjection p) {
    return new ArticleViewActivityDto(
        p.id(),
        p.viewedBy(),
        p.createdAt(),
        p.articleId(),
        p.source(),
        p.sourceUrl(),
        p.articleTitle(),
        p.articlePublishedDate(),
        p.articleSummary(),
        p.articleCommentCount(),
        p.articleViewCount()
    );
  }
}