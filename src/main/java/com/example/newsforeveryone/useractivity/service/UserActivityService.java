package com.example.newsforeveryone.useractivity.service;

import com.example.newsforeveryone.useractivity.dto.UserActivitiesResponse;

public interface UserActivityService {
  UserActivitiesResponse getUserActivities(Long userId);
}
