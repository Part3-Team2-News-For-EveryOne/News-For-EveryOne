package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.NewsArticleMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleMetricRepository extends JpaRepository<NewsArticleMetric, Long> {
}