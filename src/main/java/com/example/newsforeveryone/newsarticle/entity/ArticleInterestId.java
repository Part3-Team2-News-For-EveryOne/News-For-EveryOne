package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode
@Embeddable
public class ArticleInterestId implements Serializable {

  @Column(name = "article_id")
  private Long articleId;

  @Column(name = "interest_id")
  private Long interestId;

}
