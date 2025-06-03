package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ArticleViewId implements Serializable {

  @Column(name = "article_id")
  private Long articleId;

  @Column(name = "viewer_id")
  private Long viewerId;

  public ArticleViewId() {}

  public ArticleViewId(Long articleId, Long viewerId) {
    this.articleId = articleId;
    this.viewerId = viewerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArticleViewId)) return false;
    ArticleViewId that = (ArticleViewId) o;
    return Objects.equals(articleId, that.articleId) &&
        Objects.equals(viewerId, that.viewerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(articleId, viewerId);
  }
}
