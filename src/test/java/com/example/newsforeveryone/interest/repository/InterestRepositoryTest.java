package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.entity.Interest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


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
        Optional<Double> maxSimilarity = interestRepository.findMaxSimilarity(targetName);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(maxSimilarity).isPresent();
            softly.assertThat(maxSimilarity.get()).isGreaterThanOrEqualTo(0.8);
        });
    }

    @Transactional
    @DisplayName("임계치미만, 가장 높은 유사도를 반환합니다.")
    @Test
    void test_UnderThreshold() {
        // given
        interestRepository.save(new Interest("대한민국서울이화교중랑천산책로"));
        String targetName = "대한민국서울이화교중랑천산책길";

        // when
        Optional<Double> maxSimilarity = interestRepository.findMaxSimilarity(targetName);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(maxSimilarity).isPresent();
            softly.assertThat(maxSimilarity.get()).isLessThan(0.8);
        });
    }

    @Transactional
    @DisplayName("비교할 단어가 없다면, null을 반환합니다.")
    @Test
    void test_NotExist() {
        // given
        String targetName = "대한민국서울이화교중랑천산책길";

        // when
        Optional<Double> maxSimilarity = interestRepository.findMaxSimilarity(targetName);

        // then
        Assertions.assertThat(maxSimilarity).isEmpty();
    }

}