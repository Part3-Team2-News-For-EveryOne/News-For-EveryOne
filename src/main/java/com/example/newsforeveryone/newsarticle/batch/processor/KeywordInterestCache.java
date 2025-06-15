package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


import com.example.newsforeveryone.interest.entity.InterestKeyword;
import jakarta.annotation.PostConstruct;


import java.util.*;

@RequiredArgsConstructor
public class KeywordInterestCache {

  private final InterestKeywordRepository interestKeywordRepository;
  private Map<String, Set<Long>> keywordToInterestIds;

  public void init() {
    List<InterestKeyword> mappings = interestKeywordRepository.findAllWithKeyword();

    this.keywordToInterestIds = mappings.stream()
        .collect(Collectors.groupingBy(
            ik -> ik.getKeyword().getName(),
            Collectors.mapping(ik -> ik.getId().getInterestId(), Collectors.toSet())
        ));
  }

  public Set<Long> findInterestIdsFromContent(String content) {
    Set<Long> matchedInterestIds = new HashSet<>();

    for (Map.Entry<String, Set<Long>> entry : keywordToInterestIds.entrySet()) {
      String keyword = entry.getKey();
      if (content.contains(keyword)) {
        matchedInterestIds.addAll(entry.getValue());
      }
    }

    return matchedInterestIds;
  }
}

