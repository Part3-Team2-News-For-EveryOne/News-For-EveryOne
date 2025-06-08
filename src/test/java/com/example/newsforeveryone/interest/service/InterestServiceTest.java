package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
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

    /**
     * 사전 : 러닝머신 - 한강, 러닝 - 중랑
     * 관심사로 조회
     * 입력 : 러닝
     * 반환 : 러닝 - 중랑천, 한강
     * <p>
     * 키워드로 조회
     * 입력 : 중랑
     * 반환 : 러닝 - 중랑천, 한강
     * <p>
     * Interest-Keyword 테이블에서 찾자
     * 객체 참조가 되어있으니 request의 like만 가져옵니다.
     * 기준은 (관심사이름, 구독자 수) - (오름차순, 내림차순) 입니다.
     * <p>
     * <p>
     * 커서는 nextCursor
     * 만약에 다음 요소가 없으면  -> nextCursor, nextAfter : null
     * 일단 관심사이름, 구독자순으로 정렬하고 createdAt으로 다시 정렬합니다. 근데 이거 둘다 같은 방향으로 되야합니다. 아니면 다음게 안나와요
     * 관심사 이름 + ASC ->  nextCursor : 관심사이름, nextAfter : createdAt
     * 구독사순 + ASC ->   nextCursor : 구독자수, nextAfter : createdAt
     * 쿼리는 order By 구독자순이고(필드가 필요하긴 할수도 매번 계산해서 줄 수는 없으니깐),
     */
    // 다른 부분에서도 진행해야합니다.
    @Transactional
    @DisplayName("관심사와 키워드로 조회하면, 부분 일치하는 데이터를 반환합니다.")
    @Test
    void getInterests_SearchInterestAndKeyWord() {
        // given
        Interest savedInterest = saveInterestAndKeyword("러닝머신", "중랑천");
        Interest savedNextInterest = saveInterestAndKeyword("러닝", "한강");
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

    private Interest saveInterestAndKeyword(String interestName, String keywordName) {
        Interest savedNextInterest = interestRepository.save(new Interest(interestName));
        List<Keyword> savedNextKeywords = keywordRepository.saveAll(List.of(new Keyword(keywordName)));
        List<InterestKeyword> nextInterestKeywords = savedNextKeywords.stream()
                .map(keyword -> new InterestKeyword(savedNextInterest, keyword))
                .toList();
        interestKeywordRepository.saveAll(nextInterestKeywords);
        return savedNextInterest;
    }

    @Transactional
    @DisplayName("관심사와 키워드로 조회하면, 부분 일치하는 데이터를 반환합니다.")
    @Test
    void getInterests_SearchInterestAndKeyWord_2() {
        // given

        // when

        // then
//        Assertions.assertThat();
    }

    @Transactional
    @Test
    void getInterests_Exception() {

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