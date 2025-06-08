package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.interest.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final KeywordService keywordService;
    private final InterestRepository interestRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final SubscriptionRepository subscriptionRepository;

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

    @Override
    public SubscriptionResult subscribeInterest(long interestId, long userId) {
        return null;
    }

    @Override
    public void unsubscribeInterest(long interestId, long userId) {
    }

    @Override
    public void deleteInterestById(long interestId) {
    }

    @Override
    public InterestResult updateKeywordInInterest(long interestId) {
        return null;
    }

}
