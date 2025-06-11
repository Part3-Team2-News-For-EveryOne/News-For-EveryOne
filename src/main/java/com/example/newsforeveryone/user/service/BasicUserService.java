package com.example.newsforeveryone.user.service;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.user.dto.UserLoginRequest;
import com.example.newsforeveryone.user.dto.UserResponse;
import com.example.newsforeveryone.user.dto.UserSignupRequest;
import com.example.newsforeveryone.user.dto.UserUpdateRequest;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.user.mapper.UserMapper;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  @Transactional
  public UserResponse signup(UserSignupRequest request) {
    validateEmailNotExists(request.email());
    String encodedPassword = encodePassword(request.password());
    User user = userMapper.toEntity(request, encodedPassword);
    User savedUser = userRepository.save(user);
    log.info("User signup completed. email: {}", request.email());
    return userMapper.toResponse(savedUser);
  }

  @Override
  @Transactional
  public void softDeleteUser(Long userId, Long requestUserId) {
    validateUserPermission(userId, requestUserId);
    User user = findActiveUserById(userId);
    user.markAsDeleted();
    log.info("User logically deleted. userId: {}", userId);
  }

  @Override
  @Transactional
  public void hardDeleteUser(Long userId, Long requestUserId) {
    validateUserPermission(userId, requestUserId);
    User user = findActiveUserById(userId);
    userRepository.delete(user);
    log.info("User physically deleted. userId: {}", userId);
  }

  @Override
  @Transactional
  public UserResponse updateUserNickname(Long userId, UserUpdateRequest request, Long requestUserId) {
    validateUserPermission(userId, requestUserId);
    User user = findActiveUserById(userId);
    user.updateNickname(request.nickname());
    log.info("User nickname updated. userId: {}", userId);
    return userMapper.toResponse(user);
  }

  @Override
  public User findActiveUserById(Long userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
  }

  @Override
  public List<UserResponse> findAllActiveUsers() {
    List<User> users = userRepository.findAllByDeletedAtIsNull();
    return userMapper.toResponseList(users);
  }

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_CREDENTIALS));
  }

  // ======== 내부 메서드 ========
  private void validateEmailNotExists(String email) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
      throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
    }
  }

  private String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  private void validateUserPermission(Long userId, Long requestUserId) {
    if (!userId.equals(requestUserId)) {
      throw new BaseException(ErrorCode.UNAUTHORIZED_USER_ACCESS);
    }
  }

}
