package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

}
