package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.exception.InterestKeywordMissingException;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.interest.service.impl.KeywordService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

class KeywordServiceTest extends IntegrationTestSupport {

  @Autowired
  private KeywordRepository keywordRepository;

  @Autowired
  private KeywordService keywordService;

  @Transactional
  @DisplayName("키워드를 등록할 때, 유사도가 설정한 임계치보다 낮으면 키워드를 새로 저장하고, 보다 높으면 기존 키워드를 반환합니다.")
  @Test
  void registerKeyword() {
    // given
    keywordRepository.save(new Keyword("테스타"));
    List<String> keywords = List.of("테스트", "중랑");

    // when
    List<Keyword> registeredKeywords = keywordService.registerKeyword(keywords, 0.8);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(keywordRepository.findAll()).hasSize(3);
      softly.assertThat(registeredKeywords)
          .extracting(Keyword::getName)
          .containsExactlyInAnyOrder("테스트", "중랑");
    });
  }

  @Transactional
  @DisplayName("null또는 빈리스트가 들어왔을떄, 예외를 반환합니다.")
  @NullAndEmptySource
  @ParameterizedTest
  void registerKeyword_NullException(List<String> keywords) {
    // when & then
    Assertions.assertThatThrownBy(() -> keywordService.registerKeyword(keywords, 0.8))
        .isInstanceOf(InterestKeywordMissingException.class);
  }

}