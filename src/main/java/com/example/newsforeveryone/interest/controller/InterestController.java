package com.example.newsforeveryone.interest.controller;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/interests")
public class InterestController {

    private final InterestService interestService;

    @GetMapping
    public ResponseEntity<CursorPageInterestResponse<InterestResult>> getAllInterests(@Valid @RequestBody InterestSearchRequest interestSearchRequest,  @RequestHeader("Monew-Request-User-ID") Long userId) {
        CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(interestSearchRequest);

        return ResponseEntity.ok(interests);
    }

    @PostMapping
    public ResponseEntity<InterestResult> registerInterest(@Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
        InterestResult interestResult = interestService.registerInterest(interestRegisterRequest);

        return ResponseEntity.ok(interestResult);
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
        interestService.deleteInterestById(interestId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestResult> updateInterest(@PathVariable(name = "interestId") Long interestId) {
        InterestResult interestResult = interestService.updateInterest(interestId);

        return ResponseEntity.ok(interestResult);
    }

}
