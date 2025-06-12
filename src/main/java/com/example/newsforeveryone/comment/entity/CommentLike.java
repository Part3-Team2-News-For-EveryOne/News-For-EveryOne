package com.example.newsforeveryone.comment.entity;

import com.example.newsforeveryone.comment.entity.Id.CommentLikeId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CommentLikeId.class)
public class CommentLike {

  @Id
  @Column(name = "comment_id")
  private Long commentId;

  @Id
  @Column(name = "liked_user_id")
  private Long likedUserId;

  @Column(name = "liked_at", nullable = false)
  @Builder.Default
  private Instant likedAt = Instant.now();
}

