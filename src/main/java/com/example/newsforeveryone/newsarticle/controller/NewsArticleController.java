package com.example.newsforeveryone.newsarticle.controller;

import com.example.newsforeveryone.newsarticle.dto.ArticleRestoreResultDto;
import com.example.newsforeveryone.newsarticle.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RequiredArgsConstructor
@RequestMapping("/api/articles")
@RestController
public class NewsArticleController {

    private final NewsArticleService newsArticleService;


    @GetMapping("/restore")
    public ResponseEntity<ArticleRestoreResultDto> restoreArticle(
            @RequestParam Instant from,
            @RequestParam Instant to)
    {
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
