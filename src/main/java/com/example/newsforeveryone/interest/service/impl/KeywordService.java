package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.exception.InterestAlreadyExistException;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public List<Keyword> registerKeyword(List<String> requestKeywords, double threshold) {
    if (requestKeywords == null) {
      throw new IllegalArgumentException("키워드 값이 존재하지 않습니다.");
    }
    List<Keyword> savedKeyWords = new ArrayList<>();
    List<Keyword> newKeywords = new ArrayList<>();
    for (String keyword : requestKeywords) {
      keywordRepository.findMaxSimilarityKeyword(keyword, threshold)
          .ifPresentOrElse(savedKeyWords::add, () -> newKeywords.add(new Keyword(keyword)));
    }
    List<Keyword> savedNewKeywords = keywordRepository.saveAll(newKeywords);
    savedKeyWords.addAll(savedNewKeywords);

    return savedKeyWords;
  }

}
