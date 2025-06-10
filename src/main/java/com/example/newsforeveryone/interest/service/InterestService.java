package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.request.InterestUpdateRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;

public interface InterestService {

    InterestResult registerInterest(InterestRegisterRequest interestRegisterRequest, double threshold);

    CursorPageInterestResponse<InterestResult> getInterests(InterestSearchRequest interestSearchRequest, long userId);

    SubscriptionResult subscribeInterest(long interestId, long userId);

    void unsubscribeInterest(long interestId, long userId);

    void deleteInterest(long interestId);

    InterestResult updateKeywordInInterest(long interestId, long userId, InterestUpdateRequest interestUpdateRequest, double threshold);

}
