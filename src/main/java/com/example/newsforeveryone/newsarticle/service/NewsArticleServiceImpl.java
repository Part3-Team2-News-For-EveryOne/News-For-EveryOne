package com.example.newsforeveryone.newsarticle.service;

import com.example.newsforeveryone.newsarticle.dto.ArticleRestoreResultDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleViewDto;
import com.example.newsforeveryone.newsarticle.dto.CursorPageArticleRequest;
import com.example.newsforeveryone.newsarticle.dto.CursorPageResponseArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsArticleServiceImpl implements NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${newsforeveryone.storage.s3.bucket}")
    private String bucket;

    @Override
    public ArticleViewDto createArticleView(Long articleId, Long userId) {
        return null;
    }

    @Override
    public CursorPageResponseArticleDto findArticlePage(CursorPageArticleRequest articleRequest, Long userId) {
        return null;
    }

    @Override
    public List<String> findSources() {
        return List.of();
    }


    @Override
    @Transactional
    public ArticleRestoreResultDto restoreArticles(Instant from, Instant to) {
        // 추후 API 요청시 batch로 동작하도록 변경 필요
        List<NewsArticle> backupArticles = getS3Backup(from);

        // ---S3 백업파일과 DB 필터링 수행 -------------------------------------------
        List<NewsArticle> filteredArticles = backupArticles.stream()
                .filter(article -> article.getCreatedAt().isAfter(from) && article.getCreatedAt().isBefore(to))
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
            log.info("누락된 기사 {}개 복구 완료", saved.size());
            return new ArticleRestoreResultDto(Instant.now(), ids, (long) ids.size());
        } else {
            log.info("모든 기사가 이미 존재합니다");
            return new ArticleRestoreResultDto(Instant.now(), List.of(), 0L);
        }
    }

    @Override
    public void softDeleteArticle(Long articleId) {
        NewsArticle matchingNewsArticle = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스 기사 정보가 존재하지 않습니다."));
        matchingNewsArticle.setDeletedAt(Instant.now());
        newsArticleRepository.save(matchingNewsArticle);
    }

    @Override
    public void hardDeleteArticle(Long articleId) {
        NewsArticle matchingNewsArticle = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스 기사 정보가 존재하지 않습니다."));
        newsArticleRepository.delete(matchingNewsArticle);
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
            log.error("백업 파일 가져오기 실패", e);
            throw new RuntimeException(e);
        }
    }
}
