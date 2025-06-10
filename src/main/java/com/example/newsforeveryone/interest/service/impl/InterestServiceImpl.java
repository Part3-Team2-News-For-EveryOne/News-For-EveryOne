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
import com.example.newsforeveryone.interest.mapper.InterestMapper;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.interest.service.InterestService;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.exception.UserNotFoundException;
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
    private final InterestMapper interestMapper;

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

        List<String> keywordNames = keywords.stream().map(Keyword::getName).toList();
        return InterestResult.fromEntity(savedInterest, keywordNames, null);
    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageInterestResponse<InterestResult> getInterests(InterestSearchRequest interestSearchRequest, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));
        List<Interest> interests = getInterests(interestSearchRequest);
        boolean hasNext = interests.size() > interestSearchRequest.limit();

        List<Interest> slicedInterests = getSlicedInterest(interests, hasNext, interestSearchRequest.limit());
        Map<Interest, List<String>> groupedKeywordsByInterest = interestKeywordRepository.groupKeywordsByInterest(slicedInterests);

        return interestMapper.toCursorPageResponse(
                groupedKeywordsByInterest,
                convertWord(interestSearchRequest),
                interestSearchRequest.orderBy(),
                user,
                hasNext
        );
    }

    private List<Interest> getSlicedInterest(List<Interest> interests, boolean hasNext, int limit) {
        if (hasNext) {
            return interests.subList(0, limit);
        }

        return interests;
    }

    private List<Interest> getInterests(InterestSearchRequest interestSearchRequest) {
        return interestKeywordRepository.searchInterestByWordUsingCursor(
                convertWord(interestSearchRequest),
                interestSearchRequest.orderBy(),
                interestSearchRequest.direction(),
                interestSearchRequest.cursor(),
                interestSearchRequest.after(),
                interestSearchRequest.limit()
        );
    }

    private String convertWord(InterestSearchRequest interestSearchRequest) {
        if (interestSearchRequest.keyword() == null) {
            return "";
        }
        return interestSearchRequest.keyword();
    }

    @Transactional
    @Override
    public SubscriptionResult subscribeInterest(long interestId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));
        Subscription saveSubscription = subscriptionRepository.save(new Subscription(interest, user.getId()));
        interest.updateSubscriberCount(1);
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
            throw new UserNotFoundException(Map.of("user-id", userId));
        }
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));

        subscriptionRepository.deleteById(new SubscriptionId(interestId, userId));
        interest.updateSubscriberCount(-1);
        interestRepository.save(interest);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(Map.of("user-id", userId)));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(Map.of("interest-id", interestId)));
        interestKeywordRepository.deleteByInterest_Id(interestId);

        List<Keyword> keywords = keywordService.registerKeyword(interestUpdateRequest.keywords(), threshold);
        List<InterestKeyword> nextInterestKeywords = keywords.stream()
                .map(keyword -> new InterestKeyword(interest, keyword))
                .toList();
        interestKeywordRepository.saveAll(nextInterestKeywords);

        List<String> keywordNames = keywords.stream().map(Keyword::getName).toList();
        return InterestResult.fromEntity(interest, keywordNames, subscriptionRepository.existsById(new SubscriptionId(interestId, user.getId())));
    }

}
