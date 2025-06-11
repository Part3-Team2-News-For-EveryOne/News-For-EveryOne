package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Table(name = "article_view")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ArticleView {

  @EmbeddedId
  private ArticleViewId id;

  @Column(updatable = false)
  private Instant viewedAt;
}