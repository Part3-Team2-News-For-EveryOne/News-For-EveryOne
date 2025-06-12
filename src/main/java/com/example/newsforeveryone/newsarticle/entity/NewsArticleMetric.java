package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "news_article_metric")
public class NewsArticleMetric {

  @Id
  @Column(name = "article_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "article_id")
  private NewsArticle newsArticle;


  @Column(name = "comment_count")
  private Long commentCount;

  @Column(name = "view_count")
  private Long viewCount;
}