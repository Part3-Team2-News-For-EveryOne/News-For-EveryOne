package com.example.newsforeveryone.comment.entity;

import com.example.newsforeveryone.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq_gen")
  @SequenceGenerator(name = "comment_seq_gen", sequenceName = "comment_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "article_id", nullable = false)
  private Long articleId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder.Default
  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  @Builder.Default
  private List<CommentLike> likes = new ArrayList<>();

  public void updateContent(String content) {
    this.content = content;
  }

  public long getLikeCount() {
    return this.likeCount;
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0L, this.likeCount - 1);
  }

  public void addLike(CommentLike commentLike) {
    if (likes == null) {
      likes = new ArrayList<>();
    }
    likes.add(commentLike);
    increaseLikeCount();
  }

}


