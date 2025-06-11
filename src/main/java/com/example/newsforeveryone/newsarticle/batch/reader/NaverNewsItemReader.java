package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.newsarticle.batch.client.NaverNewsClient;
import com.example.newsforeveryone.newsarticle.batch.client.model.NaverNewsItem;
import com.example.newsforeveryone.newsarticle.batch.client.model.NaverNewsResponse;
import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Slf4j
@Component("naverReader")
@StepScope
@RequiredArgsConstructor
public class NaverNewsItemReader implements NewsItemReader {
    private final NaverNewsClient client;
    private Iterator<RawArticleDto> iterator;
    private final InterestKeywordRepository ikRepository;
    private final NewsArticleRepository articleRepository;

    // step 직전에 한번 호출
    // DB에서 모든 매핑(키워드, 관심사)를 가져오고
    // 키워듭 ㅕㄹ로 네이버 API를 호출 -> RawArticleDto로 변환
    // 기존 DB에 저장된 link의 중복 제거
    // iterator에 담아두면 read()로 하나씩 꺼내 쓴다.
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        // 모든 관심사 키워드 조회
        List<InterestKeyword> keywords = ikRepository.findAllWithKeyword();

        // 키워드 별로 set<interestId>를 묶은 Map생성
        Map<String, Set<Long>> keywordToIds = keywords.stream()
            .collect(Collectors.groupingBy(
                ik -> ik.getKeyword().getName(),
                Collectors.mapping(ik -> ik.getInterest().getId(), Collectors.toSet())
            ));

        // API 호출 결과를 담을 버퍼
        List<RawArticleDto> articleDtos = new ArrayList<>();

        // 각 키워드마다 한 번씩 네이버 뉴스 검색
        for (Map.Entry<String, Set<Long>> entry : keywordToIds.entrySet()) {
            // 키워드, 관심사 Id
            String keyword = entry.getKey();
            Set<Long> interestIds = entry.getValue();

            // display=100, start=1, sort=date(최신순)
            NaverNewsResponse resp = client.search(keyword, 100, 1, "date");

            // 응답 항목마다 RawArticleDto로 변환
            for (NaverNewsItem item : resp.getItems()) {
                articleDtos.add(new RawArticleDto(
                    "naver",          // sourceName
                    item.getOriginalLink(),      // link
                    item.getTitle(),             // title
                    item.getDescription(),       // description
                    item.getPubDate(),           // publishedAt
                    interestIds                  // 관심사 ID 목록
                ));
            }
        }

        // dto로 변환한 기사들의 link
        Set<String> allLinks = articleDtos.stream()
            .map(RawArticleDto::link)
            .collect(Collectors.toSet());

        // 이미 존재하여 겹치는 링크set
        Set<String> existing = new HashSet<>(articleRepository.findLinksByLinkIn(allLinks));

        // 겹치는 link판단 후 기사 삭제
        List<RawArticleDto> filtered = articleDtos.stream()
            .filter(dto -> !existing.contains(dto.link()))
            .distinct()   // 같은 키워드가 여러 번 검색된 결과 제거
            .toList();

        // iterator 준비
        this.iterator = filtered.iterator();
        log.info("NaverNewsItemReader 준비 완료: 총 {}건", filtered.size());
    }

    /**
     * Step이 데이터를 요청할 때마다 호출됩니다.
     * iterator에 남은 게 있으면 하나 꺼내고, 없으면 null을 반환하여 스텝 종료를 알립니다.
     */
    @Override
    public RawArticleDto read() {
        if (iterator == null || !iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }
}