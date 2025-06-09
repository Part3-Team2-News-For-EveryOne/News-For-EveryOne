package com.example.newsforeveryone.newsarticle;


import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")  // test용 DB 설정이 있으면 활성화
@Import(SecurityConfigTest.class)
public class DeleteArticleTest {
    @LocalServerPort
    private int port;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    private final RestTemplate restTemplate = new RestTemplate();

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
        String softDeleteUrl = "http://localhost:" + port + "/api/articles/" + saved.getId();
        // spring security 인증관련 부분
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(softDeleteUrl, HttpMethod.DELETE, request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<NewsArticle> updated = newsArticleRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDeletedAt()).isNotNull();

        // 생성된 뉴스기사 hard 삭제
        // 실제 DB에 news_article의 deleted_at 결과를 보고싶으면 아래 코드 주석처리 후 실행하면 됨
        String hardDeleteUrl = "http://localhost:" + port + "/api/articles/" + saved.getId() +"/hard";
        HttpHeaders headersHard = new HttpHeaders();
        HttpEntity<Void> requestHard = new HttpEntity<>(headersHard);
        restTemplate.exchange(hardDeleteUrl, HttpMethod.DELETE, requestHard, Void.class);
    }

    @Test
    @DisplayName("[NewsArticle][삭제] HardDelete 테스트")
    void hardDeleteNewsArticleTest() {
        // given
        NewsArticle article = NewsArticle.builder()
                .sourceName("코드잇일보")
                .link("https://codeitnews.com/news")
                .title("하드 삭제 테스트에요. 성공하길 빌겠습니다.")
                .summary("성공하셨어요~ 잘하셨습니다.")
                .publishedAt(Instant.now())
                .build();
        NewsArticle saved = newsArticleRepository.save(article);

        String url = "http://localhost:" + port + "/api/articles/" + saved.getId() +"/hard";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

}
