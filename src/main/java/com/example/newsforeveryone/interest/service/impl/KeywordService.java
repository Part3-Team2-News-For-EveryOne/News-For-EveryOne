package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.exception.InterestKeywordMissingException;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public List<Keyword> registerKeyword(List<String> requestKeywords, double threshold) {
    if (requestKeywords == null || requestKeywords.isEmpty()) {
      throw new InterestKeywordMissingException(Map.of());
    }

    List<Keyword> savedKeyWords = new ArrayList<>();
    List<Keyword> newKeywords = new ArrayList<>();
    JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
    for (String keyword : requestKeywords) {
      Optional<Keyword> mostSimilarityKeyword = keywordRepository.findMostSimilarityKeyword(
          keyword);
      filterWordIsPresent(threshold, savedKeyWords, newKeywords, jaroWinklerSimilarity, keyword,
          mostSimilarityKeyword);

      if (mostSimilarityKeyword.isEmpty()) {
        newKeywords.add(new Keyword(keyword));
      }
    }
    List<Keyword> savedNewKeywords = keywordRepository.saveAll(newKeywords);
    savedKeyWords.addAll(savedNewKeywords);

    return savedKeyWords;
  }

  private void filterWordIsPresent(double threshold, List<Keyword> savedKeyWords,
      List<Keyword> newKeywords,
      JaroWinklerSimilarity jaroWinklerSimilarity, String keyword,
      Optional<Keyword> maxSimilarityKeyword) {
    if (maxSimilarityKeyword.isPresent()) {
      Double apply = jaroWinklerSimilarity.apply(keyword, maxSimilarityKeyword.get().getName());
      if (apply < threshold) {
        newKeywords.add(new Keyword(keyword));
      }
      if (apply >= threshold) {
        savedKeyWords.add(maxSimilarityKeyword.get());
      }
    }
  }

}
