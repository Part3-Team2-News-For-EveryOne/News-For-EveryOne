package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

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

    @Transactional
    @DisplayName("관심사와 키워드로 조회하면, 부분 일치하는 데이터를 반환합니다.")
    @Test
    void getInterests_SearchInterestAndKeyWord() {
        // given
        Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
        Interest savedNextInterest = saveInterestAndKeyword("러닝", List.of("한강"));
        InterestSearchRequest interestSearchRequest = new InterestSearchRequest(
                savedInterest.getName(),
                "name",
                "DESC",
                null,
                null,
                1
        );

        // when
        CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(interestSearchRequest);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(interests)
                    .extracting(CursorPageInterestResponse::hasNext, CursorPageInterestResponse::nextCursor,
                            CursorPageInterestResponse::nextAfter, CursorPageInterestResponse::totalElements)
                    .containsExactlyInAnyOrder(true, savedNextInterest.getName(), savedNextInterest.getCreatedAt(), 2);
            softly.assertThat(interests.contents())
                    .extracting(InterestResult::id)
                    .containsExactlyInAnyOrder(savedInterest.getId());
        });
    }

    private Interest saveInterestAndKeyword(String interestName, List<String> keywordNames) {
        Interest savedNextInterest = interestRepository.save(new Interest(interestName));
        List<Keyword> keywords = keywordNames.stream().map(Keyword::new).toList();
        List<Keyword> savedNextKeywords = keywordRepository.saveAll(keywords);
        List<InterestKeyword> nextInterestKeywords = savedNextKeywords.stream()
                .map(keyword -> new InterestKeyword(savedNextInterest, keyword))
                .toList();
        interestKeywordRepository.saveAll(nextInterestKeywords);
        return savedNextInterest;
    }

    @Transactional
    @DisplayName("사용자가 관심사를 구독합니다.")
    @Test
    void subscribeInterest() {
        // given
        User savedUser = userRepository.save(new User("", "", ""));
        Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

        // when
        SubscriptionResult subscriptionResult = interestService.subscribeInterest(savedInterest.getId(), savedUser.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(subscriptionResult)
                    .extracting(SubscriptionResult::interestId, SubscriptionResult::interestKeyword, SubscriptionResult::interestSubscriberCount)
                    .containsExactlyInAnyOrder(savedInterest.getId(), List.of("중랑천"), 1);
            softly.assertThat(subscriptionResult.id()).isEqualTo(new SubscriptionId(savedInterest.getId(), savedUser.getId()));
        });
    }

    @Transactional
    @DisplayName("등록되지 않은 사용자가 관심사를 구독하면, 예외를 반환합니다.")
    @Test
    void subscribeInterest_NoUserException() {
        // given
        Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

        // when & then
        Assertions.assertThatThrownBy(() -> interestService.subscribeInterest(savedInterest.getId(), -1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Transactional
    @DisplayName("등록되지 않은 관심사를 구독한다면, 예외를 반환합니다.")
    @Test
    void subscribeInterest_NoInterestException() {
        // given
        User savedUser = userRepository.save(new User("", "", ""));

        // when & then
        Assertions.assertThatThrownBy(() -> interestService.subscribeInterest(-1L, savedUser.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("관심사 구독을 취소합니다.")
    @Test
    void unsubscribeInterest() {
        // given
        User savedUser = userRepository.save(new User("", "", ""));
        Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
        SubscriptionResult subscriptionResult = interestService.subscribeInterest(savedInterest.getId(), savedUser.getId());

        // when
        interestService.unsubscribeInterest(savedInterest.getId(), savedUser.getId());

        // then
        // subScrpitpn 에서 없어야합니다.

    }

    @Test
    void deleteInterestById() {

    }

    @Test
    void updateInterest() {

    }

}