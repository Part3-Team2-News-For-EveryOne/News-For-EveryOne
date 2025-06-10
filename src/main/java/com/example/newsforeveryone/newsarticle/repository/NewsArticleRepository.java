package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
  @Query("select a.link from NewsArticle a where a.link in :links")
  List<String> findLinksByLinkIn(@Param("links") Set<String> links);

  @Query("SELECT a FROM NewsArticle a WHERE a.publishedAt BETWEEN :from AND :to")
  List<NewsArticle> findAllByCreatedAtBetween(Instant from, Instant to);

  List<NewsArticle> findAllByLinkIn(List<String> links);
}
