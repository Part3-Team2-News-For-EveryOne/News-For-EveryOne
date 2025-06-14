package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.request.InterestUpdateRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.example.newsforeveryone.interest.exception.InterestNotFoundException;
import com.example.newsforeveryone.interest.mapper.InterestMapper;
import com.example.newsforeveryone.interest.mapper.SubscriptionMapper;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.interest.service.InterestService;
import com.example.newsforeveryone.interest.service.WordSimilarityService;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.exception.UserNotFoundException;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private static final double SIMILARITY_THRESHOLD = 0.8;
  private final KeywordService keywordService;
  private final WordSimilarityService wordSimilarityService;
  private final InterestRepository interestRepository;
  private final InterestKeywordRepository interestKeywordRepository;
  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final InterestMapper interestMapper;
  private final SubscriptionMapper subscriptionMapper;

  @Transactional
  @Override
  public InterestResult registerInterest(InterestRegisterRequest interestRegisterRequest) {
    wordSimilarityService.validateSimilarity(interestRegisterRequest.name(), SIMILARITY_THRESHOLD);

    // 키워드가 빈 리스트 이면 종료
    Interest savedInterest = interestRepository.save(new Interest(interestRegisterRequest.name()));
    List<Keyword> savedKeywords = keywordService.registerKeyword(interestRegisterRequest.keywords(),
        SIMILARITY_THRESHOLD);
    List<Keyword> keywords = saveKeywordsByInterest(savedInterest, savedKeywords);

    return interestMapper.toResult(savedInterest, keywords, null);
  }

  @Transactional(readOnly = true)
  @Override
  public CursorPageInterestResponse<InterestResult> getInterests(
      InterestSearchRequest interestSearchRequest, long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));

    Slice<Interest> interests = findInterestsWithCursor(interestSearchRequest);

    return interestMapper.toCursorPageResponse(
        interests,
        interestSearchRequest.searchWord(),
        interestSearchRequest.orderBy(),
        user.getId()
    );
  }

  @Transactional
  @Override
  public SubscriptionResult subscribeInterest(long interestId, long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));

    Subscription saveSubscription = subscriptionRepository.save(new Subscription(interest, user));
    interest.increaseSubscriberCount();
    interestRepository.save(interest);

    return subscriptionMapper.toResult(interestId, saveSubscription);
  }

  @Transactional
  @Override
  public void unsubscribeInterest(long interestId, long userId) {
    validateUserExists(userId);
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));

    subscriptionRepository.deleteById(new SubscriptionId(interestId, userId));
    interest.decreaseSubscriberCount();
    interestRepository.save(interest);
  }

  @Transactional
  @Override
  public void deleteInterest(long interestId) {
    validateInterestExists(interestId);

    interestKeywordRepository.deleteByInterest_Id(interestId);
    subscriptionRepository.deleteByInterest_Id(interestId);
    interestRepository.deleteById(interestId);
  }

  @Transactional
  @Override
  public InterestResult updateKeywordInInterest(long interestId, long userId,
      InterestUpdateRequest interestUpdateRequest) {
    validateUserExists(userId);
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));

    interestKeywordRepository.deleteByInterest_Id(interestId);
    List<Keyword> savedKeywords = keywordService.registerKeyword(interestUpdateRequest.keywords(),
        SIMILARITY_THRESHOLD);
    List<Keyword> keywords = saveKeywordsByInterest(interest, savedKeywords);

    return interestMapper.toResult(interest, keywords, userId);
  }

  private List<Keyword> saveKeywordsByInterest(Interest savedInterest,
      List<Keyword> savedKeywords) {
    List<InterestKeyword> interestKeywords = savedKeywords.stream()
        .map(keyword -> new InterestKeyword(savedInterest, keyword))
        .toList();
    interestKeywordRepository.saveAll(interestKeywords);

    return savedKeywords;
  }

  private void validateUserExists(long userId) {
    if (userRepository.existsById(userId)) {
      return;
    }
    throw new UserNotFoundException(Map.of("user-id", userId));
  }

  private void validateInterestExists(long interestId) {
    if (interestRepository.existsById(interestId)) {
      return;
    }
    throw new InterestNotFoundException(Map.of("interest-id", interestId));
  }

  private Slice<Interest> findInterestsWithCursor(InterestSearchRequest interestSearchRequest) {
    return interestRepository.searchInterestByWordWithCursor(
        interestSearchRequest.searchWord(),
        interestSearchRequest.orderBy(),
        interestSearchRequest.direction(),
        interestSearchRequest.cursor(),
        interestSearchRequest.after(),
        interestSearchRequest.limit()
    );
  }

}
