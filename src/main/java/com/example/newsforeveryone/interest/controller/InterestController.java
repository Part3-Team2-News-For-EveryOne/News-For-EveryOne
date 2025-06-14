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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/interests")
public class InterestController {

  private final InterestService interestService;

  @PostMapping
  public ResponseEntity<InterestResult> registerInterest(
      @Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
    InterestResult interestResult = interestService.registerInterest(interestRegisterRequest);

    return ResponseEntity.ok(interestResult);
  }

  @GetMapping
  public ResponseEntity<CursorPageInterestResponse<InterestResult>> getAllInterests(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam @NotBlank String orderBy,
      @RequestParam @NotBlank String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") Integer limit,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    InterestSearchRequest interestSearchRequest = InterestSearchRequest.of(keyword, orderBy,
        direction, cursor, after, limit);
    CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(
        interestSearchRequest, userDetails.getUserId());

    return ResponseEntity.ok(interests);
  }

  @PostMapping("/{interestId}/subscriptions")
  public ResponseEntity<SubscriptionResult> subscribeInterest(
      @PathVariable(name = "interestId") Long interestId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    SubscriptionResult subscriptionResult = interestService.subscribeInterest(interestId,
        userDetails.getUserId());

    return ResponseEntity.ok(subscriptionResult);
  }

  @DeleteMapping("/{interestId}/subscriptions")
  public ResponseEntity<Void> unsubscribeInterest(
      @PathVariable(name = "interestId") Long interestId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    interestService.unsubscribeInterest(interestId, userDetails.getUserId());

    return ResponseEntity.noContent()
        .build();
  }

  @DeleteMapping("/{interestId}")
  public ResponseEntity<Void> deleteInterest(@PathVariable(name = "interestId") Long interestId) {
    interestService.deleteInterest(interestId);

    return ResponseEntity.noContent()
        .build();
  }

  @PatchMapping("/{interestId}")
  public ResponseEntity<InterestResult> updateInterest(
      @PathVariable(name = "interestId") Long interestId,
      @Valid @RequestBody InterestUpdateRequest interestUpdateRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    InterestResult interestResult = interestService.updateKeywordInInterest(interestId,
        userDetails.getUserId(), interestUpdateRequest);

    return ResponseEntity.ok(interestResult);
  }

}
