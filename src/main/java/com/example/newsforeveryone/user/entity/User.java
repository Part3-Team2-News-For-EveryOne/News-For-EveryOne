package com.example.newsforeveryone.user.entity;

import com.example.newsforeveryone.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 100)
  private String nickname;

  @Column(nullable = false)
  private String password;

  @Builder
  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

}
