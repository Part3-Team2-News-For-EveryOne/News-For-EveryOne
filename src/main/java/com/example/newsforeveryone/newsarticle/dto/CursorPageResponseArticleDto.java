package com.example.newsforeveryone.newsarticle.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseArticleDto(
    List<ArticleDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {

}
