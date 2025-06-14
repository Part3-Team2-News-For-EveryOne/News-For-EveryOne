package com.example.newsforeveryone.interest.mapper;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterestMapper {

  private final SubscriptionRepository subscriptionRepository;
  private final InterestKeywordRepository interestKeywordRepository;

  public InterestResult toResult(Interest interest, List<Keyword> keywords, Long userId) {
    if (userId == null) {
      return InterestResult.fromEntity(interest, keywords, null);
    }
    SubscriptionId subscriptionId = new SubscriptionId(interest.getId(), userId);
    boolean exists = subscriptionRepository.existsById(subscriptionId);

    return InterestResult.fromEntity(interest, keywords, exists);
  }

  public CursorPageInterestResponse<InterestResult> toCursorPageResponse(
      Slice<Interest> interests,
      String requestWord,
      String orderBy,
      Long userId
  ) {
    List<InterestResult> interestResults = getInterestResults(interests.getContent(), userId);
    long totalElement = interestKeywordRepository.countInterestAndKeywordsBySearchWord(requestWord);

    return CursorPageInterestResponse.fromEntity(
        interestResults,
        getNextCursor(interests.getContent(), orderBy),
        getNextAfter(interests.getContent()),
        totalElement,
        interests.hasNext()
    );
  }

  private List<InterestResult> getInterestResults(
      List<Interest> interests,
      Long userId
  ) {
    Map<Interest, List<Keyword>> groupedKeywordsByInterest = interestKeywordRepository
        .findKeywordsByInterests(interests)
        .stream()
        .collect(Collectors.groupingBy(
            InterestKeyword::getInterest,
            Collectors.mapping(InterestKeyword::getKeyword, Collectors.toList())
        ));
    Set<SubscriptionId> userSubscribedIds = findUserSubscriptions(interests, userId);

    return interests
        .stream()
        .map(interest -> toInterestResult(
            interest,
            groupedKeywordsByInterest,
            userId,
            userSubscribedIds
        ))
        .toList();
  }

  private Set<SubscriptionId> findUserSubscriptions(List<Interest> interests, Long userId) {
    Set<SubscriptionId> subscriptionIds = interests
        .stream()
        .map(interest -> new SubscriptionId(interest.getId(), userId))
        .collect(Collectors.toSet());

    return subscriptionRepository.findAllById(subscriptionIds)
        .stream()
        .map(Subscription::getId)
        .collect(Collectors.toSet());
  }

  private InterestResult toInterestResult(
      Interest interest,
      Map<Interest, List<Keyword>> groupedKeywordsByInterest,
      Long userId,
      Set<SubscriptionId> subscribedIds
  ) {
    boolean isSubscribed = subscribedIds.contains(new SubscriptionId(interest.getId(), userId));
    return InterestResult.fromEntity(
        interest,
        groupedKeywordsByInterest.get(interest),
        isSubscribed
    );
  }

  private String getNextAfter(List<Interest> interests) {
    if (interests.isEmpty()) {
      return null;
    }
    Interest lastInterest = interests.get(interests.size() - 1);

    return lastInterest.getCreatedAt()
        .toString();
  }

  private String getNextCursor(List<Interest> interests, String orderBy) {
    if (interests.isEmpty()) {
      return null;
    }
    Interest lastInterest = interests.get(interests.size() - 1);
    if (orderBy.equals("subscriberCount")) {
      return String.valueOf(lastInterest.getSubscriberCount());
    }

    return lastInterest.getName();
  }

}
