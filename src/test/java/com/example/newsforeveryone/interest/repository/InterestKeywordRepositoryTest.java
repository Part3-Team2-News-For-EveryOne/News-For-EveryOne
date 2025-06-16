package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;


class InterestKeywordRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private InterestKeywordRepository interestKeywordRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private KeywordRepository keywordRepository;

  @Transactional
  @DisplayName("관심사와 관심사에 속하는 키워드를 매핑합니다.")
  @Test
  void groupKeywordsByInterest() {
    // given
    Interest savedFirstInterest = saveInterestAndKeyword("천", List.of("군산", "서울"));
    Interest savedSecondInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    List<Interest> savedInterests = List.of(savedFirstInterest, savedSecondInterest);

    // when
    List<InterestKeyword> interestKeywords = interestKeywordRepository.findKeywordsByInterests(
        savedInterests);

    // then
    Assertions.assertThat(interestKeywords)
        .extracting(
            interestKeyword -> interestKeyword.getInterest().getName(),
            interestKeyword -> interestKeyword.getKeyword().getName()
        )
        .containsExactlyInAnyOrder(
            Tuple.tuple("러닝머신", "중랑천"),
            Tuple.tuple("천", "군산"),
            Tuple.tuple("천", "서울")
        );
  }

  @Transactional
  @DisplayName("검색어가 관심사 또는 키워드 이름에 포함될 경우 관련 관삼사 수를 카운트한다")
  @Test
  void countInterestsBySearchWord() {
    // given
    saveInterestAndKeyword("광진구", List.of("건대", "한양대", "학교"));
    saveInterestAndKeyword("대학교", List.of("구"));

    String targetName = "구";

    // when
    long count = interestKeywordRepository.countInterestAndKeywordsBySearchWord(targetName);

    // then
    Assertions.assertThat(count).isEqualTo(2);
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