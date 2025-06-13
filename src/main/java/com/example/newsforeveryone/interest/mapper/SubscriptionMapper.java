package com.example.newsforeveryone.interest.mapper;

import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {

  private final InterestKeywordRepository interestKeywordRepository;

  public SubscriptionResult toResult(long interestId, Subscription saveSubscription) {
    List<InterestKeyword> interestKeywords = interestKeywordRepository.findByInterest_Id(
        interestId);
    List<String> keywords = interestKeywords.stream()
        .map(InterestKeyword::getKeyword)
        .map(Keyword::getName)
        .toList();

    return SubscriptionResult.fromEntity(saveSubscription, keywords);
  }

}
