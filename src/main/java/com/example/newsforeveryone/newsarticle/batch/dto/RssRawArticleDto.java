package com.example.newsforeveryone.newsarticle.batch.dto;

import java.time.Instant;

public record RssRawArticleDto(
    String sourceName,
    String link,
    String title,
    String summary,
    String author,
    Instant publishedAt
) {

}
