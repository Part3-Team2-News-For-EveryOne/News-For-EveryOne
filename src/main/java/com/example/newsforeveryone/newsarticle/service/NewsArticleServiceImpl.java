package com.example.newsforeveryone.newsarticle.service;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.newsarticle.dto.ArticleRestoreResultDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleViewDto;
import com.example.newsforeveryone.newsarticle.dto.CursorPageArticleRequest;
import com.example.newsforeveryone.newsarticle.dto.CursorPageResponseArticleDto;
import com.example.newsforeveryone.newsarticle.entity.ArticleView;
import com.example.newsforeveryone.newsarticle.entity.ArticleViewId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NewsArticleServiceImpl implements NewsArticleService {

  private final NewsArticleRepository newsArticleRepository;
  private final NewsArticleQueryRepository newsArticleQueryRepository;
  private final ArticleViewRepository articleViewRepository;
  private final SourceRepository sourceRepository;
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;

  @Value("${newsforeveryone.storage.s3.bucket}")
  private String bucket;


  @Override
  @Transactional
  public ArticleViewDto createArticleView(Long articleId, Long userId) {
    NewsArticle article = newsArticleRepository.findById(articleId)
        .orElseThrow(() -> new BaseException(
            ErrorCode.ARTICLE_NOT_FOUND, Map.of("articleId", articleId)));

    ArticleViewId viewId = new ArticleViewId(articleId, userId);
    ArticleView articleView = new ArticleView(viewId, Instant.now());
    articleViewRepository.save(articleView);

    long viewCount = articleViewRepository.countById_ArticleId(articleId);
    long commentCount = commentRepository.countByArticleIdAndDeletedAtIsNull(articleId);

    return ArticleViewDto.from(article, articleView, commentCount, viewCount);
  }

  @Override
  public CursorPageResponseArticleDto findArticlePage(CursorPageArticleRequest articleRequest,
      Long userId) {
    return newsArticleQueryRepository.findArticlePage(articleRequest, userId);
  }

  @Override
  public List<String> findSources() {
    return sourceRepository.findAllNames();
  }

  @Override
  @Transactional
  public ArticleRestoreResultDto restoreArticles(Instant from, Instant to) {
    // 추후 API 요청시 batch로 동작하도록 변경 필요
    List<NewsArticle> backupArticles = getS3Backup(from);

    // ---S3 백업파일과 DB 필터링 수행 -------------------------------------------
    List<NewsArticle> filteredArticles = backupArticles.stream()
        .filter(
            article -> article.getCreatedAt().isAfter(from) && article.getCreatedAt().isBefore(to))
        .toList();

    List<String> filteredLinks = filteredArticles.stream().map(NewsArticle::getLink).toList();

    Set<String> existingLinks = newsArticleRepository.findAllByLinkIn(filteredLinks)
        .stream().map(NewsArticle::getLink).collect(Collectors.toSet());

    List<NewsArticle> missingArticles = filteredArticles.stream()
        .filter(article -> !existingLinks.contains(article.getLink()))
        .toList();

        // ---누락 기사 복구 수행 ----------------------------------------------------
        if (!missingArticles.isEmpty()) {
            missingArticles.forEach(article -> article.setId(null));
            List<NewsArticle> saved = newsArticleRepository.saveAll(missingArticles);
            List<Long> ids = saved.stream().map(NewsArticle::getId).toList();
            log.info("{} missing articles have been restored", saved.size());
            return new ArticleRestoreResultDto(Instant.now(), ids, (long) ids.size());
        } else {
            log.info("All articles already exist");
            return new ArticleRestoreResultDto(Instant.now(), List.of(), 0L);
        }
    }

    @Override
    public void softDeleteArticle(Long articleId) {
        NewsArticle matchingNewsArticle = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new BaseException(ErrorCode.ARTICLE_NOT_FOUND, Map.of("articleId", articleId)));
        matchingNewsArticle.setDeletedAt(Instant.now());
        newsArticleRepository.save(matchingNewsArticle);
        log.info("Article logically deleted. articleId: {}", articleId);
    }

    @Override
    public void hardDeleteArticle(Long articleId) {
        NewsArticle matchingNewsArticle = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new BaseException(ErrorCode.ARTICLE_NOT_FOUND, Map.of("articleId", articleId)));
        newsArticleRepository.delete(matchingNewsArticle);
        log.info("Article physically deleted. articleId: {}", articleId);
    }

  // ===== 내부 메서드 =====
  private List<NewsArticle> getS3Backup(Instant from) {
    // AWS S3 백업 파일 가져오기
    try {
      LocalDate date = from.atZone(ZoneOffset.UTC).toLocalDate();
      String key = String.format("news-backup/%s/articles_%s.json", date, date);

      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

            InputStream inputStream = s3Client.getObject(getObjectRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            List<NewsArticle> backupArticles = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                backupArticles.add(objectMapper.readValue(line, NewsArticle.class));
            }
            return backupArticles;
        } catch (Exception e) {
            log.error("Failed to retrieve backup file", e);
            throw new RuntimeException(e);
        }
    }
}
