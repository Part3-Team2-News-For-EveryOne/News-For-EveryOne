package com.example.newsforeveryone.newsarticle.controller;

import com.example.newsforeveryone.newsarticle.dto.ArticleViewDto;
import com.example.newsforeveryone.newsarticle.dto.CursorPageArticleRequest;
import com.example.newsforeveryone.newsarticle.dto.CursorPageResponseArticleDto;
import com.example.newsforeveryone.newsarticle.service.NewsArticleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newsforeveryone.newsarticle.dto.ArticleRestoreResultDto;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RequiredArgsConstructor
@RequestMapping("/api/articles")
@RestController
public class NewsArticleController {

  private final NewsArticleService newsArticleService;

  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> createArticleView(
      @PathVariable Long articleId,
      @RequestHeader("Monew-Request-User-ID") Long requestUserId) {

    ArticleViewDto articleViewDto = newsArticleService.createArticleView(articleId, requestUserId);
    return ResponseEntity.ok(articleViewDto);
  }

  @GetMapping("/sources")
  public ResponseEntity<List<String>> getSources() {
    List<String> sources = newsArticleService.findSources();
    return ResponseEntity.ok(sources);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseArticleDto> findArticlePage(
      @Validated @ModelAttribute CursorPageArticleRequest request,
      @RequestHeader("Monew-Request-User-ID") Long userId
  ) {
    CursorPageResponseArticleDto response = newsArticleService.findArticlePage(request, userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/restore")
  public ResponseEntity<ArticleRestoreResultDto> restoreArticle(
      @RequestParam Instant from,
      @RequestParam Instant to
  ) {
    ArticleRestoreResultDto result = newsArticleService.restoreArticles(from, to);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> softDelete(@PathVariable("id") Long newsArticleId) {
    newsArticleService.softDeleteArticle(newsArticleId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<Void> hardDelete(@PathVariable("id") Long newsArticleId) {
    newsArticleService.hardDeleteArticle(newsArticleId);
    return ResponseEntity.noContent().build();
  }
}
