package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
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
            throw new IllegalArgumentException("해당 관심사와 유사한 관심사가 이미 있습니다.");
        }
        Interest interest = new Interest(interestRegisterRequest.name());
        Interest savedInterest = interestRepository.save(interest);

        List<Keyword> keywords = keywordService.registerKeyword(interestRegisterRequest.keywords(), threshold);
        List<InterestKeyword> interestKeywords = keywords.stream()
                .map(keyword -> new InterestKeyword(interest, keyword))
                .toList();
        interestKeywordRepository.saveAll(interestKeywords);

        return InterestResult.fromEntity(savedInterest, keywords, 0, null);
    }

    // TODO: 6/8/25 보류
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
                .orElseThrow(() -> new IllegalArgumentException(""));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new IllegalArgumentException(""));
        Subscription saveSubscription = subscriptionRepository.save(new Subscription(interest, user.getId()));
        interest.addSubscriberCount(1);

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
            throw new IllegalArgumentException("");
        }
        if (!interestRepository.existsById(interestId)) {
            throw new IllegalArgumentException("");
        }
        subscriptionRepository.deleteById(new SubscriptionId(interestId, userId));
    }

    @Override
    public void deleteInterestById(long interestId) {

    }

    @Override
    public InterestResult updateKeywordInInterest(long interestId) {
        return null;
    }

}
