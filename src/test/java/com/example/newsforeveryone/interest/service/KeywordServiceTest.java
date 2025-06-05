package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.interest.service.impl.KeywordService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        keywordRepository.save(new Keyword("대한민국서울이화교중랑천산책로"));
        List<String> keywords = List.of("대한민국서울이화교중랑천산책", "중랑천", "이화교");

        // when
        List<Keyword> registeredKeywords = keywordService.registerKeyword(keywords, 0.8);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(keywordRepository.findAll()).hasSize(3);
            softly.assertThat(registeredKeywords)
                    .extracting(Keyword::getName)
                    .containsExactlyInAnyOrder("대한민국서울이화교중랑천산책로", "중랑천", "이화교");

        });
    }

    @Transactional
    @DisplayName("null이 들어왔을떄, 예외를 반환합니다.")
    @Test
    void registerKeyword_NullException() {
        // given
        List<String> keywords = null;

        // when & then
        Assertions.assertThatThrownBy(() -> keywordService.registerKeyword(keywords, 0.8))
                .isInstanceOf(IllegalArgumentException.class);
    }

}