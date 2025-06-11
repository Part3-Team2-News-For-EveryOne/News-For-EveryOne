package com.example.newsforeveryone.user.controller;

import com.example.newsforeveryone.user.config.auth.CustomUserDetails;
import com.example.newsforeveryone.user.dto.UserLoginRequest;
import com.example.newsforeveryone.user.dto.UserResponse;
import com.example.newsforeveryone.user.dto.UserSignupRequest;
import com.example.newsforeveryone.user.dto.UserUpdateRequest;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.mapper.UserMapper;
import com.example.newsforeveryone.user.repository.UserRepository;
import com.example.newsforeveryone.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
    UserResponse response = userService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponse> login(
      @Valid @RequestBody UserLoginRequest request,
      HttpServletRequest httpRequest
  ) {
    // 1. Spring Security 인증 수행
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    // 2. SecurityContext에 인증 정보 저장
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 3. 세션 생성 (명시적 생성)
    HttpSession session = httpRequest.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

    // 4. 응답 반환
    User user = userService.findUserByEmail(request.email());
    return ResponseEntity.ok(userMapper.toResponse(user));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    userService.softDeleteUser(userId, userDetails.getUserId());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(@PathVariable Long userId) {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    userService.hardDeleteUser(userId, userDetails.getUserId());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable Long userId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    UserResponse response = userService.updateUserNickname(userId, request, userDetails.getUserId());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<UserResponse> users = userService.findAllActiveUsers();
    return ResponseEntity.ok(users);
  }
}
