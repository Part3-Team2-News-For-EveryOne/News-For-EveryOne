package com.example.newsforeveryone.interest.controller;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.request.InterestUpdateRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 시큐리티로 수정 필요
@RestController
@RequiredArgsConstructor
@RequestMapping("api/interests")
public class InterestController {

    private static final double SIMILARITY_THRESHOLD = 0.8;
    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestResult> registerInterest(@Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
        InterestResult interestResult = interestService.registerInterest(interestRegisterRequest, SIMILARITY_THRESHOLD);

        return ResponseEntity.ok(interestResult);
    }

    @GetMapping
    public ResponseEntity<CursorPageInterestResponse<InterestResult>> getAllInterests(@Valid @RequestBody InterestSearchRequest interestSearchRequest, @RequestHeader("Monew-Request-User-ID") Long userId) {
        CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(interestSearchRequest);

        return ResponseEntity.ok(interests);
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionResult> subscribeInterest(@PathVariable(name = "interestId") Long interestId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        SubscriptionResult subscriptionResult = interestService.subscribeInterest(interestId, userId);

        return ResponseEntity.ok(subscriptionResult);
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribeInterest(@PathVariable(name = "interestId") Long interestId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        interestService.unsubscribeInterest(interestId, userId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> deleteInterest(@PathVariable(name = "interestId") Long interestId) {
        interestService.deleteInterest(interestId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestResult> updateInterest(@PathVariable(name = "interestId") Long interestId, @RequestHeader("Monew-Request-User-ID") Long userId, @Valid @RequestBody InterestUpdateRequest interestUpdateRequest) {
        InterestResult interestResult = interestService.updateKeywordInInterest(interestId, userId, interestUpdateRequest, SIMILARITY_THRESHOLD);

        return ResponseEntity.ok(interestResult);
    }

}
