package com.example.newsforeveryone.user.controller;

import com.example.newsforeveryone.user.dto.UserLoginRequest;
import com.example.newsforeveryone.user.dto.UserResponse;
import com.example.newsforeveryone.user.dto.UserSignupRequest;
import com.example.newsforeveryone.user.dto.UserUpdateRequest;
import com.example.newsforeveryone.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
    UserResponse response = userService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
    UserResponse response = userService.login(request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId, @RequestHeader("MoNew-Request-User-ID") Long requestUserId) {
    userService.softDeleteUser(userId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(@PathVariable Long userId, @RequestHeader("MoNew-Request-UserId") Long requestUserId) {
    userService.hardDeleteUser(userId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request, @RequestHeader("MoNew-Request-UserId") Long requestUserId) {
    UserResponse response = userService.updateUserNickname(userId, request, requestUserId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<UserResponse> users = userService.findAllActiveUsers();
    return ResponseEntity.ok(users);
  }

}
