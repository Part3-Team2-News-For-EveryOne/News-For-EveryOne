package com.example.newsforeveryone.comment.entity;

import com.example.newsforeveryone.common.entity.SoftDeletableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  @Builder.Default
  private List<CommentLike> likes = new ArrayList<>();

  public void updateContent(String content) {
    this.content = content;
  }

  public long getLikeCount() {
    return likes != null ? likes.size() : 0;
  }

  public void addLike(CommentLike commentLike) {
    if (likes == null) {
      likes = new ArrayList<>();
    }
    likes.add(commentLike);
  }

  public void removeLike(CommentLike commentLike) {
    if (likes != null) {
      likes.remove(commentLike);
    }
  }
}