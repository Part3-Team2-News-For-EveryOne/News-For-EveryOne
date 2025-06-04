package com.example.newsforeveryone.newsarticle.batch.dto;

import java.time.Instant;
import java.util.Objects;

public record RssRawArticleDto(
    String sourceName,
    String link,
    String title,
    String summary,
    String author,
    Instant publishedAt
) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RssRawArticleDto other)) return false;
    return Objects.equals(this.link, other.link);
  }

  @Override
  public int hashCode() {
    return Objects.hash(link);
  }
}
