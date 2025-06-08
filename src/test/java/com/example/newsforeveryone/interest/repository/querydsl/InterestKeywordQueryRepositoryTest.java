package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.IntegrationTestSupport;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


class InterestKeywordQueryRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private InterestKeywordRepository interestKeywordRepository;
    @Autowired
    private InterestRepository interestRepository;
    @Autowired
    private KeywordRepository keywordRepository;

    @Transactional
    @DisplayName("~이면, ~이다")
    @Test
    void test_arr() {
        // given
        Interest savedOtherInterest = saveInterestAndKeyword("천", List.of("군산", "서울"));
        Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천", "중랑"));
        Interest savedNextInterest = saveInterestAndKeyword("러닝", List.of("면목천", "면목"));

        // when
        Map<Interest, List<String>> interestListMap = interestKeywordRepository.searchByWord(
                "천", "name", "DESC", null, null, 2
        );

        // then
        Assertions.assertThat(interestListMap.entrySet())
                .extracting(interestListEntry -> interestListEntry.getKey().getName(), Map.Entry::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("러닝", List.of("면목천", "면목")),
                        Tuple.tuple("러닝머신", List.of("중랑천", "중랑"))
//                            Tuple.tuple("천", List.of("군산", "서울"))
                );
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