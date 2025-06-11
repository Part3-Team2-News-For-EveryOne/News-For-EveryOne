package com.example.newsforeveryone.newsarticle.dto;

import java.time.Instant;
import java.util.List;

public record ArticleRestoreResultDto(
    Instant restoreDate,
    List<Long> restoreArticleIds,
    Long restoredArticleCount
) {

}
