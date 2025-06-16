package com.example.newsforeveryone.newsarticle.batch.dto;

import jakarta.xml.bind.annotation.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class HankyungRssItemDto implements ArticleItemNormalizer {

  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "link")
  private String link;

  @XmlElement(name = "author")
  private String author;

  @XmlElement(name = "pubDate")
  private String pubDate;

  @Override
  public String getNormalizedSourceName() {
    return "한경RSS";
  }

  @Override
  public String getNormalizedTitle() {
    return title;
  }

  @Override
  public String getNormalizedLink() {
    return link;
  }

  @Override
  public String getNormalizedSummary() {
    // 한경은 description 없으므로 title로 대체
    return title;
  }

  @Override
  public String getNormalizedAuthor() {
    return author;
  }

  @Override
  public Instant getNormalizedPublishedAt() {
    return (this.pubDate == null) ? null : ZonedDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
  }
}
