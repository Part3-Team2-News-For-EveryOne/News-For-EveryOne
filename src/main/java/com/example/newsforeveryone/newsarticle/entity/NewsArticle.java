package com.example.newsforeveryone.newsarticle.entity;

import com.example.newsforeveryone.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

@Getter
@ToString
@Table(name = "news_article")
@Entity
public class NewsArticle extends BaseEntity {

  @Column(name = "source_name", length = 500, nullable = false)
  private String sourceName;

  @Column(name = "link", length = 1024, unique = true, nullable = false)
  private String link;

  @Column(name = "title", length = 500, nullable = false)
  private String title;

  @Column(name = "summary")
  private String summary;

  @Formula("(SELECT COUNT(*) FROM article_view av WHERE av.article_id = id)") // N + 1!!!!
  private int viewCount;

  @Column(name = "published_at", nullable = false)
  private Instant publishedAt;

}
