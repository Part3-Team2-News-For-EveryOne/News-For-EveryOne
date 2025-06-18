package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.exception.InterestAlreadyExistException;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.service.WordSimilarityService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WordSimilarityServiceImpl implements WordSimilarityService {

  private final InterestRepository interestRepository;

  @Override
  public void validateSimilarity(String word, double threshold) {
    JaroWinklerSimilarity jw = new JaroWinklerSimilarity();
    Optional<Interest> mostSimilarInterest = interestRepository.findMostSimilarInterest(word);
    if (mostSimilarInterest.isEmpty()) {
      return;
    }
    double similarity = jw.apply(word, mostSimilarInterest.get().getName());
    if (similarity < threshold) {
      return;
    }

    throw new InterestAlreadyExistException(Map.of("word", word, "similarity", similarity));
  }

}
