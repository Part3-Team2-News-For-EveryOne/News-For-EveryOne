package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


class InterestKeywordCustomTest extends IntegrationTestSupport {

  @Autowired
  private InterestKeywordRepository interestKeywordRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private KeywordRepository keywordRepository;

  @Transactional
  @DisplayName("검색 조건에 따라 요청된 양의 데이터를 가져옵니다.")
  @MethodSource("provideSearchConditions")
  @ParameterizedTest
  void testSearch(String searchWord, String orderBy, String direction, String cursor, String after,
      int limit, List<String> expectedResults) {
    // given
    Interest savedFirstInterest = saveInterestAndKeyword("천", List.of("군산", "서울"));
    Interest savedSecondInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    Interest savedThridInterest = saveInterestAndKeyword("러닝", List.of("면목천", "면목"));
    savedFirstInterest.increaseSubscriberCount();
    interestRepository.save(savedFirstInterest);
    String afterSubCursor = getAfter(after, savedFirstInterest);

    // when
    List<Interest> interests = interestKeywordRepository.searchInterestByWordUsingCursor(
        searchWord, orderBy, direction, cursor, afterSubCursor, limit
    );

    // then
    Assertions.assertThat(interests)
        .extracting(Interest::getName)
        .containsExactlyElementsOf(expectedResults);
  }

  private static Stream<Arguments> provideSearchConditions() {
    return Stream.of(
        // 커서가 없는 경우
        Arguments.of("천", "name", "DESC", null, null, 1, List.of("천", "러닝머신")),
        Arguments.of("천", "name", "asc", null, null, 1, List.of("러닝", "러닝머신")),
        Arguments.of("천", "subscriberCount", "desc", null, null, 1, List.of("천", "러닝")),
        Arguments.of("천", "subscriberCount", "asc", null, null, 1, List.of("러닝머신", "러닝")),
        // 커서가 존재할떄
        Arguments.of("천", "name", "DESC", "러닝머신", "CREATED_AT", 1, List.of("러닝")),
        Arguments.of("천", "name", "asc", "러닝머신", "CREATED_AT", 1, List.of("천")),
        Arguments.of("천", "subscriberCount", "desc", "1", "CREATED_AT", 1, List.of("러닝", "러닝머신")),
        Arguments.of("천", "subscriberCount", "asc", "0", "CREATED_AT", 1, List.of("러닝머신", "러닝"))
    );
  }

  @Transactional
  @DisplayName("관심사와 관심사에 속하는 키워드를 매핑해준다")
  @Test
  void groupKeywordsByInterest() {
    // given
    Interest savedFirstInterest = saveInterestAndKeyword("천", List.of("군산", "서울"));
    Interest savedSecondInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

    // when
    Map<Interest, List<String>> interestListMap = interestKeywordRepository.groupKeywordsByInterest(
        List.of(savedFirstInterest, savedSecondInterest));

    // then
    Assertions.assertThat(interestListMap.entrySet())
        .extracting(entry -> entry.getKey().getName(), Map.Entry::getValue)
        .containsExactlyElementsOf(
            List.of(
                Tuple.tuple("천", List.of("군산", "서울")),
                Tuple.tuple("러닝머신", List.of("중랑천"))
            )
        );
  }

  @Transactional
  @DisplayName("관심사 리스트가 null이거나 비어있을 경우 빈 결과를 반환한다")
  @NullAndEmptySource
  @ParameterizedTest
  void groupKeywordsByInterest_NullException(List<Interest> input) {
    // when
    Map<Interest, List<String>> interestListMap = interestKeywordRepository.groupKeywordsByInterest(
        input);

    // then
    Assertions.assertThat(interestListMap.entrySet()).isEmpty();
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