package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


class InterestRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private InterestRepository interestRepository;

  @Transactional
  @DisplayName("임계치이상, 가장 높은 유사도를 반환합니다.")
  @Test
  void test_TopSimilarity() {
    // given
    interestRepository.save(new Interest("대한민국서울이화교중랑천산책로"));
    String targetName = "대한민국서울이화교중랑천산책";

    // when
    Double maxSimilarity = interestRepository.findMaxSimilarity(targetName);

    // then
    Assertions.assertThat(maxSimilarity).isGreaterThanOrEqualTo(0.8);
  }

  @Transactional
  @DisplayName("임계치미만, 가장 높은 유사도를 반환합니다.")
  @Test
  void test_UnderThreshold() {
    // given
    interestRepository.save(new Interest("대한민국서울이화교중랑천산책로"));
    String targetName = "대한민국서울이화교중랑천산책길";

    // when
    Double maxSimilarity = interestRepository.findMaxSimilarity(targetName);

    // then
    Assertions.assertThat(maxSimilarity).isLessThan(0.8);
  }

}