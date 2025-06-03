package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;


@Getter
@Table(name = "article_view")
@Entity
public class ArticleView {

  @EmbeddedId
  private ArticleViewId id;

  // DB에 외래키로 설정되어 있음
  @Column(name = "article_id", nullable = false, insertable = false, updatable = false)
  private Long articleId;

  // DB에 외래키로 설정되어 있음
  @Column(name = "viewer_id", nullable = false, insertable = false, updatable = false)
  private Long viewerId;

  private Instant viewedAt;
}