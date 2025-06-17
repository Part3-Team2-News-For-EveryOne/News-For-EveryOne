package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;


class InterestRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private KeywordRepository keywordRepository;
  @Autowired
  private InterestKeywordRepository interestKeywordRepository;


  @Transactional
  @DisplayName("검색 조건에 따라 요청된 양의 데이터를 가져옵니다.")
  @MethodSource("provideSearchConditions")
  @ParameterizedTest
  void searchInterestByWordWithCursor(String searchWord, String orderBy, String direction,
      String cursor, String after,
      int limit, List<String> expectedResults
  ) {
    // given
    Interest savedFirstInterest = saveInterestAndKeyword("천", List.of("군산", "서울"));
    saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    saveInterestAndKeyword("러닝", List.of("면목천", "면목"));
    savedFirstInterest.increaseSubscriberCount();
    interestRepository.save(savedFirstInterest);
    String afterSubCursor = getAfter(after, savedFirstInterest);

    // when
    Slice<Interest> interests = interestRepository.searchInterestByWordWithCursor(
        searchWord, orderBy, direction, cursor, afterSubCursor, limit
    );

    // then
    Assertions.assertThat(interests.getContent())
        .extracting(Interest::getName)
        .containsExactlyElementsOf(expectedResults);
  }

  private static Stream<Arguments> provideSearchConditions() {
    return Stream.of(
        // 커서가 없는 경우
        Arguments.of("천", "name", "DESC", null, null, 2, List.of("천", "러닝머신")),
        Arguments.of("천", "name", "asc", null, null, 2, List.of("러닝", "러닝머신")),
        Arguments.of("천", "subscriberCount", "desc", null, null, 2, List.of("천", "러닝")),
        Arguments.of("천", "subscriberCount", "asc", null, null, 2, List.of("러닝머신", "러닝")),
        // 커서가 존재할떄
        Arguments.of("천", "name", "DESC", "러닝머신", "CREATED_AT", 2, List.of("러닝")),
        Arguments.of("천", "name", "asc", "러닝머신", "CREATED_AT", 2, List.of("천")),
        Arguments.of("천", "subscriberCount", "desc", "1", "CREATED_AT", 2, List.of("러닝", "러닝머신")),
        Arguments.of("천", "subscriberCount", "asc", "0", "CREATED_AT", 2, List.of("러닝머신", "러닝"))
    );
  }

  @Transactional
  @DisplayName("임계치이상, 가장 높은 유사도를 가진 단어를 반환합니다.")
  @Test
  void test_TopSimilarityWord() {
    // given
    String otherName = "유사도검사테스타";
    String similarName = "대한민국서울이화교중랑천산책";
    Interest interest = interestRepository.save(new Interest("대한민국서울이화교중랑천산책로"));
    interestRepository.save(new Interest(otherName));

    // when
    Interest mostSimilarInterest = interestRepository.findMostSimilarInterest(similarName);

    // then
    Assertions.assertThat(mostSimilarInterest.getName()).isEqualTo(interest.getName());
  }

  private String getAfter(String after, Interest savedFirstInterest) {
    if (after == null) {
      return null;
    }

    return savedFirstInterest.getCreatedAt().toString();
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

}