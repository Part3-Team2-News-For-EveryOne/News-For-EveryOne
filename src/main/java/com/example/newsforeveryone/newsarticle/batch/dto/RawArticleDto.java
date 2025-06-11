package com.example.newsforeveryone.newsarticle.batch.dto;

import java.time.Instant;
import java.util.Set;

public record RawArticleDto(
    String sourceName,
    String link,
    String title,
    String description,
    Instant publishedAt,
    // naver API는 키워드로 기사수집 -> 따라서 어떤 키웓로 수집했는지 기록 필요
    Set<Long> interestIds
) {

}
