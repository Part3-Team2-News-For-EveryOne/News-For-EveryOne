package com.example.newsforeveryone.newsarticle.batch.dto;

import jakarta.xml.bind.annotation.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Getter
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChosunRssItemDto implements ArticleItemNormalizer {

  @XmlElement(name = "title")
  private String title;

  @XmlElement(name = "link")
  private String link;

  @XmlElement(name = "encoded", namespace = "http://purl.org/rss/1.0/modules/content/")
  private String contentEncoded;

  @XmlElement(name = "creator", namespace = "http://purl.org/dc/elements/1.1/")
  private String dcCreator;

  @XmlElement(name = "pubDate")
  private String pubDate;


  @Override
  public String getNormalizedSourceName() {
    return "조선RSS";
  }

  @Override
  public String getNormalizedTitle() {
    return this.title;
  }

  @Override
  public String getNormalizedLink() {
    return this.link;
  }

  @Override
  public String getNormalizedSummary() {
    String html = this.contentEncoded;
    if (html != null) {
      int start = html.indexOf("<p>");
      int end = html.indexOf("</p>");
      if (start != -1 && end != -1) {
        return html.substring(start + 3, end);
      }
    }
    return "";
  }

  @Override
  public String getNormalizedAuthor() {
    return this.dcCreator;
  }

  @Override
  public Instant getNormalizedPublishedAt() {
    return (this.pubDate == null) ? null : ZonedDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
  }
}