package com.example.newsforeveryone.interest.controller;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.request.InterestUpdateRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.service.InterestService;
import com.example.newsforeveryone.user.config.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {

    private static final double SIMILARITY_THRESHOLD = 0.8;
    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestResult> registerInterest(@Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
        InterestResult interestResult = interestService.registerInterest(interestRegisterRequest, SIMILARITY_THRESHOLD);

        return ResponseEntity.ok(interestResult);
    }

    @GetMapping
    public ResponseEntity<CursorPageInterestResponse<InterestResult>> getAllInterests(@Valid @ModelAttribute InterestSearchRequest interestSearchRequest) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(interestSearchRequest, userDetails.getUserId());

        return ResponseEntity.ok(interests);
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionResult> subscribeInterest(@PathVariable(name = "interestId") Long interestId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        SubscriptionResult subscriptionResult = interestService.subscribeInterest(interestId, userDetails.getUserId());

        return ResponseEntity.ok(subscriptionResult);
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribeInterest(@PathVariable(name = "interestId") Long interestId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        interestService.unsubscribeInterest(interestId, userDetails.getUserId());

        return ResponseEntity.noContent()
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
    public ResponseEntity<InterestResult> updateInterest(@PathVariable(name = "interestId") Long interestId, @Valid @RequestBody InterestUpdateRequest interestUpdateRequest) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        InterestResult interestResult = interestService.updateKeywordInInterest(interestId, userDetails.getUserId(), interestUpdateRequest, SIMILARITY_THRESHOLD);

        return ResponseEntity.ok(interestResult);
    }

}
