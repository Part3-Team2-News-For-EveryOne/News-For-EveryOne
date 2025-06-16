package com.example.newsforeveryone.newsarticle.batch.dto;

import jakarta.xml.bind.annotation.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class YonhapRssItemDto implements ArticleItemNormalizer {

  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "link")
  private String link;

  @XmlElement(name = "description")
  private String description;

  @XmlElement(name = "creator", namespace = "http://purl.org/dc/elements/1.1/")
  private String dcCreator;

  @XmlElement(name = "pubDate")
  private String pubDate;

  @Override
  public String getNormalizedSourceName() {
    return "연합RSS";
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
    return description;
  }

  @Override
  public String getNormalizedAuthor() {
    return dcCreator;
  }

  @Override
  public Instant getNormalizedPublishedAt() {
    return (this.pubDate == null) ? null : ZonedDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
  }
}
