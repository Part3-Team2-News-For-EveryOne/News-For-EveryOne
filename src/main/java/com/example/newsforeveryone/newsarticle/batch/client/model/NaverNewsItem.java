package com.example.newsforeveryone.newsarticle.batch.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverNewsItem {
    private String title;
    // naver API가 제공해주는 link
    private String link;
    private String description;

    // 사용자에게 보여질 최종 링크
    // naverAPI 필드명
    @JsonProperty("originallink")
    private String originalLink;

    @JsonProperty("pubDate")
    private String pubDateRaw;

    // 편의 메서드: RFC-1123 포맷(pubDateRaw) → Instant 변환
    @JsonProperty("pubDateRaw")
    public Instant getPubDate() {
        return ZonedDateTime
            .parse(pubDateRaw, DateTimeFormatter.RFC_1123_DATE_TIME)
            .toInstant();
    }
}
