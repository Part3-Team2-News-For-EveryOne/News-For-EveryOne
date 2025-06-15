package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.common.exception.external.RateLimitExceededException;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.newsarticle.batch.client.NaverNewsClient;
import com.example.newsforeveryone.newsarticle.batch.client.model.NaverXmlResponse;
import com.example.newsforeveryone.newsarticle.batch.dto.NaverItemDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NaverItemReader implements ItemReader<NaverItemDto> {

  private final NaverNewsClient client;
  private final KeywordRepository keywordRepository;

  private List<Keyword> keywords;
  private int currentKeywordIndex;
  private List<NaverItemDto> itemBuffer;
  private int currentItemIndexInBuffer;

  @BeforeStep
  public void init(StepExecution stepExecution) {
    this.keywords = getRandomKeywords(100);
    this.currentKeywordIndex = 0;
    this.itemBuffer = new ArrayList<>();
    this.currentItemIndexInBuffer = 0;
    log.info("네이버에서 {}개의 키워드 검색 예정", keywords.size());
  }

  @Override
  public NaverItemDto read() {
    if (currentItemIndexInBuffer >= itemBuffer.size()) {
      fetchItemsForNextKeyword();
    }

    if (itemBuffer.isEmpty() || currentItemIndexInBuffer >= itemBuffer.size()) {
      log.info("네이버 키워드 검색 완료");
      return null;
    }

    return itemBuffer.get(currentItemIndexInBuffer++);
  }

  private void fetchItemsForNextKeyword() {
    if (currentKeywordIndex >= keywords.size()) {
      return;
    }

    Keyword currentKeyword = keywords.get(currentKeywordIndex);

    int retryCount = 0;

    while (true) {
      try {
        NaverXmlResponse response = client.searchXml(currentKeyword.getName(), 100, 1, "date");

        if (hasValidItems(response)) {
          itemBuffer = response.getItems();
        } else {
          itemBuffer.clear();
        }
        currentItemIndexInBuffer = 0;

        currentKeywordIndex++;
        break;

      } catch (RateLimitExceededException e) {
        retryCount++;
        if (retryCount >= 2) {
          throw new RuntimeException("Naver API 속도 제한으로 재시도 후에도 실패했습니다.", e);
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      }
    }

  }

  private boolean hasValidItems(NaverXmlResponse response) {
    return response != null && response.getItems() != null && !response.getItems().isEmpty();
  }

  private List<Keyword> getRandomKeywords(int limit) {
    List<Long> allIds = keywordRepository.findAllIds();
    Collections.shuffle(allIds);

    List<Long> sampledIds = allIds.stream()
        .limit(limit)
        .toList();

    Set<Keyword> keywordSet = keywordRepository.findByIdIn(sampledIds);
    return new ArrayList<>(keywordSet);
  }
}