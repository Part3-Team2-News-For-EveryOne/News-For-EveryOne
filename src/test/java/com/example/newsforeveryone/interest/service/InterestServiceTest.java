package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

class InterestServiceTest extends IntegrationTestSupport {

    @Autowired
    private InterestRepository interestRepository;
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private InterestKeywordRepository interestKeywordRepository;
    @Autowired
    private InterestService interestService;

    @Transactional
    @DisplayName("관심사와 키워드를 입력할 경우, 관심사를 등록할 수 있습니다.")
    @Test
    void registerInterest() {
        // given
        String interestName = UUID.randomUUID().toString();
        String keyword = UUID.randomUUID().toString();
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest(interestName, List.of(keyword));

        // when
        InterestResult interestResult = interestService.registerInterest(interestRegisterRequest, 0.8);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(interestResult)
                    .extracting(InterestResult::interestName, InterestResult::subscriberCount, InterestResult::subscribedByMe, InterestResult::keywords)
                    .containsExactlyInAnyOrder(interestName, 0, null, List.of(keyword));
        });
    }

    @Transactional
    @DisplayName("임계치를 넘는 유사도의 관심사가 있을 경우, 등록할 수 없습니다.")
    @Test
    void registerInterest_SimilarityOverThresholdException() {
        // given
        String interestName = "대한민국서울이화교중랑천산책로";
        String similarNewInterestName = "대한민국서울이화교중랑천산책";
        interestRepository.save(new Interest(interestName));
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest(similarNewInterestName, List.of(UUID.randomUUID().toString()));

        // when & then
        Assertions.assertThatThrownBy(() -> interestService.registerInterest(interestRegisterRequest, 0.8))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getInterests() {
    }

    @Test
    void subscribeInterest() {
    }

    @Test
    void unsubscribeInterest() {
    }

    @Test
    void deleteInterestById() {
    }

    @Test
    void updateInterest() {
    }

}