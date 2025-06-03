package com.example.newsforeveryone.newsarticle.dto;

import java.time.Instant;

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

}