package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.ArticleBackupHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleBackupHistoryRepository extends JpaRepository<ArticleBackupHistory, Long> {
}
