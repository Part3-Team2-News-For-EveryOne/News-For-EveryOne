package com.example.newsforeveryone.newsarticle.dto;

import com.example.newsforeveryone.newsarticle.entity.ArticleView;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ArticleViewDto(
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
  public static ArticleViewDto from(NewsArticle article, ArticleView articleView,
      Long commentCount, long viewCount) {

    return new ArticleViewDto(
        article.getId(),
        articleView.getId().getViewerId(),
        articleView.getViewedAt(),
        article.getId(),
        article.getSourceName(),
        article.getLink(),
        article.getTitle(),
        article.getPublishedAt(),
        article.getSummary(),
        commentCount,
        viewCount
    );
  }
}