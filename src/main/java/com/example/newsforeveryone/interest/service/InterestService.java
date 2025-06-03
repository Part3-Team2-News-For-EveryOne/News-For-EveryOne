package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;

public interface InterestService {

    CursorPageInterestResponse<InterestResult> getInterests(InterestSearchRequest interestSearchRequest);

    InterestResult registerInterest(InterestRegisterRequest interestRegisterRequest);

    SubscriptionResult subscribeInterest(long interestId, long userId);

    void unsubscribeInterest(long interestId, long userId);

    void deleteInterestById(long interestId);

    InterestResult updateInterest(long interestId);

}
