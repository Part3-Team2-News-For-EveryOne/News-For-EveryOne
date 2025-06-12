package com.example.newsforeveryone.comment.entity;

import com.example.newsforeveryone.comment.entity.id.CommentLikeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
