package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.exception.InterestAlreadyExistException;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WordSimilarityServiceTest extends IntegrationTestSupport {

  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private WordSimilarityService wordSimilarityService;

  @AfterEach
  void tearDown() {
    interestRepository.deleteAllInBatch();
  }

  @DisplayName("임계치를 넘는 유사도의 관심사가 있을 경우, 예외를 반환합니다.")
  @Test
  void similarityOverThresholdException() {
    // given
    String sameInterestName = "유사도검사테스트";
    String otherName = "유사도검사테스타";
    interestRepository.save(new Interest(sameInterestName));

    // when & then
    Assertions.assertThatThrownBy(
            () -> wordSimilarityService.validateSimilarity(otherName, 0.8))
        .isInstanceOf(InterestAlreadyExistException.class);
  }

}