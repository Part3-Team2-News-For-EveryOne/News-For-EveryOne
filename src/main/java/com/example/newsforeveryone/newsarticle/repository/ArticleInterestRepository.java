package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.ArticleInterest;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleInterestRepository extends JpaRepository<ArticleInterest, Long> {

  @Query("""
        SELECT ai.id
        FROM ArticleInterest ai
        WHERE ai.id.articleId IN (
            SELECT a.id
            FROM NewsArticle a
            WHERE a.createdAt > :since
        )
      """)
  List<ArticleInterestId> findIdsByCreatedAtAfter(@Param("since") Instant since);

  void deleteAllById_InterestId(Long interestId);

}
