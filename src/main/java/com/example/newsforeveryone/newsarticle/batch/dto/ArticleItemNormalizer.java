package com.example.newsforeveryone.newsarticle.batch.dto;

import java.time.Instant;

public interface ArticleItemNormalizer {
  String getNormalizedSourceName();

  String getNormalizedTitle();

  String getNormalizedLink();

  String getNormalizedSummary();

  String getNormalizedAuthor();

  Instant getNormalizedPublishedAt();
}
