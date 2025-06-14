package com.example.newsforeveryone.comment.entity.id;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentLikeId implements Serializable {

  private Long commentId;
  private Long likedUserId;
}
