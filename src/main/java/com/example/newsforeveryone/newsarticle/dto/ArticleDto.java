package com.example.newsforeveryone.newsarticle.dto;

import com.example.newsforeveryone.newsarticle.repository.projection.ArticleProjection;
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
    Boolean viewedByMe,
    Instant createdAt
) {
  public static ArticleDto from(ArticleProjection p) {
    return new ArticleDto(
        p.id(),
        p.source(),
        p.sourceUrl(),
        p.title(),
        p.publishDate(),
        p.summary(),
        p.commentCount(),
        p.viewCount(),
        p.viewedByMe(),
        p.createdAt()
    );
  }
}
