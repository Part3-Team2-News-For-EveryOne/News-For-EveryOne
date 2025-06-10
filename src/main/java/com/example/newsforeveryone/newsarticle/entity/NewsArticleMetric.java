package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "news_article_metric")
public class NewsArticleMetric {

  @Id
  @Column(name = "article_id")
  private Long articleId;

  @Column(name = "comment_count")
  private Long commentCount;

  @Column(name = "view_count")
  private Long viewCount;
}