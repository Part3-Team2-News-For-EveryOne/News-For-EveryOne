package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.InterestKeywordId;
import com.example.newsforeveryone.interest.entity.Keyword;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Disabled
class InterestKeywordRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private InterestKeywordRepository interestKeywordRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Transactional
    @DisplayName("%단순 확인 테스트(배포시 삭제)% : Interest와 Keyword를 저장하면 InterestKeyword에 올바른 복합키가 매핑됩니다.")
    @Test
    void simple_test() {
        // given
        Interest interest = interestRepository.save(new Interest("코드잇"));
        Keyword keyword = keywordRepository.save(new Keyword("개발자"));

        // when
        InterestKeyword savedMappingJoin = interestKeywordRepository.save(new InterestKeyword(interest, keyword));

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(interest.getId()).isNotNull();
            softly.assertThat(savedMappingJoin)
                    .extracting(InterestKeyword::getId)
                    .extracting(InterestKeywordId::getInterestId, InterestKeywordId::getKeywordId)
                    .containsExactlyInAnyOrder(interest.getId(), keyword.getId());

        });
    }

}