package com.example.newsforeveryone.user.config.auth;

import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
  private final Long userId;

  public CustomUserDetails(String email, String password, Long userId) {
    super(email, password, AuthorityUtils.createAuthorityList("USER"));
    this.userId = userId;
  }

}

