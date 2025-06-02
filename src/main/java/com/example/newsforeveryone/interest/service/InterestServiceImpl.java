package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService{

    private final InterestRepository interestRepository;

    @Override
    public CursorPageInterestResponse<InterestResult> getInterests(InterestSearchRequest interestSearchRequest) {
        return null;
    }

    @Override
    public InterestResult registerInterest(InterestRegisterRequest interestRegisterRequest) {
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
    public InterestResult updateInterest(long interestId) {
        return null;
    }
}
