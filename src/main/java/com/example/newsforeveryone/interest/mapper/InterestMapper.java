package com.example.newsforeveryone.interest.mapper;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
      Map<Interest, List<String>> interestListMap,
      String word,
      String orderBy,
      User user,
      boolean hasNext
  ) {
    List<InterestResult> interestResults = getInterestResults(interestListMap, user);

    String nextCursor = getNextCursor(interestListMap, orderBy);
    String nextAfter = getNextAfter(interestListMap);
    Long totalElement = interestKeywordRepository.countSearchInterest(word,
        interestListMap.keySet().stream().toList());

    return CursorPageInterestResponse.fromEntity(
        interestResults,
        nextCursor,
        nextAfter,
        totalElement,
        hasNext
    );
  }

  private List<InterestResult> getInterestResults(Map<Interest, List<String>> interestListMap,
      User user) {
    Set<SubscriptionId> subscriptionIds = interestListMap.keySet()
        .stream()
        .map(Interest::getId)
        .map(interestId -> new SubscriptionId(interestId, user.getId()))
        .collect(Collectors.toSet());
    Set<SubscriptionId> subscribedIds = subscriptionRepository.findAllById(subscriptionIds)
        .stream()
        .map(Subscription::getId)
        .collect(Collectors.toSet());

    return interestListMap.entrySet()
        .stream()
        .map(entry -> getInterestResult(user, subscribedIds, entry))
        .toList();
  }

  private String getNextAfter(Map<Interest, List<String>> interestListMap) {
    if (interestListMap.isEmpty()) {
      return null;
    }
    Set<Interest> interests = interestListMap.keySet();
    List<Interest> interestList = new ArrayList<>(interests);
    Interest last = interestList.get(interestList.size() - 1);

    return last.getCreatedAt().toString();
  }

  private String getNextCursor(Map<Interest, List<String>> interestListMap, String orderBy) {
    if (interestListMap.isEmpty()) {
      return null;
    }

    Set<Interest> interests = interestListMap.keySet();
    List<Interest> interestList = new ArrayList<>(interests);
    Interest last = interestList.get(interestList.size() - 1);
    if (orderBy.equals("subscriberCount")) {
      return String.valueOf(last.getSubscriberCount());
    }

    return String.valueOf(last.getName());
  }

  private InterestResult getInterestResult(User user, Set<SubscriptionId> subscribedIds,
      Map.Entry<Interest, List<String>> entry) {
    Interest interest = entry.getKey();
    List<String> keywords = entry.getValue();
    boolean isSubscribed = subscribedIds.contains(
        new SubscriptionId(interest.getId(), user.getId()));

    return InterestResult.from(interest, keywords, isSubscribed);
  }

}
