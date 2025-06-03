package com.example.newsforeveryone.newsarticle.service;

import com.example.newsforeveryone.newsarticle.dto.ArticleRestoreResultDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleViewDto;
import com.example.newsforeveryone.newsarticle.dto.CursorPageArticleRequest;
import com.example.newsforeveryone.newsarticle.dto.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;

public interface NewsArticleService {
  ArticleViewDto createArticleView(Long articleId, Long userId);
  CursorPageResponseArticleDto findArticlePage(CursorPageArticleRequest articleRequest, Long userId);
  List<String> findSources();
  ArticleRestoreResultDto restoreArticles(Instant from, Instant to);
  void softDeleteArticle(Long articleId);
  void hardDeleteArticle(Long articleId);
}
