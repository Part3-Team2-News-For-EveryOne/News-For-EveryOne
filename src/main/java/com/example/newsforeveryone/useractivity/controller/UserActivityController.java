package com.example.newsforeveryone.useractivity.controller;

import com.example.newsforeveryone.useractivity.dto.UserActivitiesResponse;
import com.example.newsforeveryone.useractivity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

  private final UserActivityService userActivityService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserActivitiesResponse> getUserActivities(@PathVariable Long userId) {
    UserActivitiesResponse activities = userActivityService.getUserActivities(userId);
    return ResponseEntity.ok(activities);
  }
}