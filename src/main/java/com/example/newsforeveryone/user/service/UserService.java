package com.example.newsforeveryone.user.service;

import com.example.newsforeveryone.user.dto.UserLoginRequest;
import com.example.newsforeveryone.user.dto.UserResponse;
import com.example.newsforeveryone.user.dto.UserSignupRequest;
import com.example.newsforeveryone.user.dto.UserUpdateRequest;
import com.example.newsforeveryone.user.entity.User;
import java.util.List;

public interface UserService {

  UserResponse signup(UserSignupRequest request);
  UserResponse login(UserLoginRequest request);
  void softDeleteUser(Long userId, Long requestUserId);
  void hardDeleteUser(Long userId, Long requestUserId);
  UserResponse updateUserNickname(Long userId, UserUpdateRequest request, Long requestUserId);
  User findActiveUserById(Long userId);
  List<UserResponse> findAllActiveUsers();

}
