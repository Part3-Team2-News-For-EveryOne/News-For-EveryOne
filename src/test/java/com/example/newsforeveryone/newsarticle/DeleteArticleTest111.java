package com.example.newsforeveryone.newsarticle;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.newsarticle.service.NewsArticleService;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DeleteArticleTest111 extends IntegrationTestSupport {

  @PersistenceContext
  EntityManager em;

  @Autowired
  private NewsArticleService newsArticleService;

  @Autowired
  private NewsArticleRepository newsArticleRepository;

  @Transactional
  @Test
  @DisplayName("[NewsArticle][삭제] SoftDelete 테스트")
  void softDeleteNewsArticleTest() {
    // given
    NewsArticle article = NewsArticle.builder()
        .sourceName("한경")
        .link("https://example.com/news")
        .title("소프트 Delete 테스트 기사")
        .summary("테스트 기사입니다")
        .publishedAt(Instant.now())
        .build();
    NewsArticle saved = newsArticleRepository.save(article);

    // when
    newsArticleService.softDeleteArticle(saved.getId());
    em.flush();
    em.clear();

    // then
    Optional<NewsArticle> updated = newsArticleRepository.findById(saved.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getSourceName()).isEqualTo("한경");
    assertThat(updated.get().getLink()).isEqualTo("https://example.com/news");
    assertThat(updated.get().getTitle()).isEqualTo("소프트 Delete 테스트 기사");
    assertThat(updated.get().getDeletedAt()).isNotNull();
  }
}
