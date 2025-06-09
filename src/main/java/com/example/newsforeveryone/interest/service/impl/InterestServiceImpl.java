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
import com.example.newsforeveryone.interest.exception.InterestAlreadyExistException;
import com.example.newsforeveryone.interest.exception.InterestNotFoundException;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.interest.service.InterestService;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final KeywordService keywordService;
    private final InterestRepository interestRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Override
    public InterestResult registerInterest(InterestRegisterRequest interestRegisterRequest, double threshold) {
        Optional<Double> interestSimilarity = interestRepository.findMaxSimilarity(interestRegisterRequest.name());
        if (interestSimilarity.isPresent() && interestSimilarity.get() >= threshold) {
            throw new InterestAlreadyExistException(Map.of("name", interestRegisterRequest.name(), "similarity", interestSimilarity.get()));
        }
        Interest interest = new Interest(interestRegisterRequest.name());
        Interest savedInterest = interestRepository.save(interest);

        List<Keyword> keywords = keywordService.registerKeyword(interestRegisterRequest.keywords(), threshold);
        List<InterestKeyword> interestKeywords = keywords.stream()
                .map(keyword -> new InterestKeyword(interest, keyword))
                .toList();
        interestKeywordRepository.saveAll(interestKeywords);

        return InterestResult.fromEntity(savedInterest, keywords, null);
    }

    // TODO: 6/8/25 보류
    @Transactional(readOnly = true)
    @Override
    public CursorPageInterestResponse<InterestResult> getInterests(InterestSearchRequest interestSearchRequest) {
        Map<Interest, List<String>> interestListMap = interestKeywordRepository.searchByWord(
                interestSearchRequest.keyword(),
                interestSearchRequest.orderBy(),
                interestSearchRequest.direction(),
                interestSearchRequest.cursor(),
                interestSearchRequest.after(),
                interestSearchRequest.limit()
        );

        return null;
    }

    @Transactional
    @Override
    public SubscriptionResult subscribeInterest(long interestId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));
        Subscription saveSubscription = subscriptionRepository.save(new Subscription(interest, user.getId()));
        interest.addSubscriberCount(1);
        interestRepository.save(interest);

        List<InterestKeyword> interestKeywords = interestKeywordRepository.findByInterest_Id(interestId);
        List<String> keywords = interestKeywords.stream()
                .map(InterestKeyword::getKeyword)
                .map(Keyword::getName)
                .toList();

        return SubscriptionResult.fromEntity(saveSubscription, keywords);
    }

    @Transactional
    @Override
    public void unsubscribeInterest(long interestId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("해당 유저가 없습니다.");
        }
        if (!interestRepository.existsById(interestId)) {
            throw new InterestNotFoundException(Map.of("interest-id", interestId));
        }
        subscriptionRepository.deleteById(new SubscriptionId(interestId, userId));
    }

    @Transactional
    @Override
    public void deleteInterest(long interestId) {
        if (!interestRepository.existsById(interestId)) {
            throw new InterestNotFoundException(Map.of("interest-id", interestId));
        }
        interestKeywordRepository.deleteByInterest_Id(interestId);
        subscriptionRepository.deleteByInterest_Id(interestId);
        interestRepository.deleteById(interestId);
    }

    @Transactional
    @Override
    public InterestResult updateKeywordInInterest(long interestId, long userId, InterestUpdateRequest interestUpdateRequest, double threshold) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));
        interestKeywordRepository.deleteByInterest_Id(interestId);

        List<Keyword> keywords = keywordService.registerKeyword(interestUpdateRequest.keywords(), threshold);
        List<InterestKeyword> nextInterestKeywords = keywords.stream()
                .map(keyword -> new InterestKeyword(interest, keyword))
                .toList();
        interestKeywordRepository.saveAll(nextInterestKeywords);

        return InterestResult.fromEntity(interest, keywords, subscriptionRepository.existsById(new SubscriptionId(interestId, userId)));
    }

}
