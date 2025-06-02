package com.example.newsforeveryone.user.mapper;

import com.example.newsforeveryone.user.dto.UserResponse;
import com.example.newsforeveryone.user.dto.UserSignupRequest;
import com.example.newsforeveryone.user.entity.User;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId().toString(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt().toString()
    );
  }

  public User toEntity(UserSignupRequest request){
    return User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();
  }

  public List<UserResponse> toResponseList(List<User> users) {
    return users.stream()
        .map(this::toResponse)
        .toList();
  }

}
