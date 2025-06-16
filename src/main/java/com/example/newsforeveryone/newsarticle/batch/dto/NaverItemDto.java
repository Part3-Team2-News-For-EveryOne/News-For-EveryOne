package com.example.newsforeveryone.newsarticle.batch.dto;

import jakarta.xml.bind.annotation.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class NaverItemDto implements ArticleItemNormalizer {

  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "originallink")
  private String originalLink; // 원본 기사 링크

  @XmlElement(name = "description")
  private String description;

  @XmlElement(name = "pubDate")
  private String pubDate;

  @Override
  public String getNormalizedSourceName() {
    return "naver";
  }

  @Override
  public String getNormalizedTitle() {
    return this.title;
  }

  @Override
  public String getNormalizedLink() {
    return this.originalLink;
  }

  @Override
  public String getNormalizedSummary() {
    return this.description;
  }

  @Override
  public String getNormalizedAuthor() {
    return null;
  }

  @Override
  public Instant getNormalizedPublishedAt() {
    if (this.pubDate == null || this.pubDate.isEmpty()) {
      return Instant.now();
    }
    return ZonedDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
  }
}