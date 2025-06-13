package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.exception.InterestAlreadyExistException;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.service.WordSimilarityService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WordSimilarityServiceImpl implements WordSimilarityService {

  private final InterestRepository interestRepository;

  @Override
  public void validateSimilarity(String word, double threshold) {
    Double interestSimilarity = interestRepository.findMaxSimilarity(word);
    if (interestSimilarity < threshold) {
      return;
    }

    throw new InterestAlreadyExistException(Map.of("word", word, "similarity", interestSimilarity));
  }

}
