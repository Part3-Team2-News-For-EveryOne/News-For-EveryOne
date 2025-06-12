package com.example.newsforeveryone.comment.mapper;

import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.comment.entity.CommentLike;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

  public Comment toEntity(CommentCreateRequest request) {
    return Comment.builder()
        .articleId(Long.valueOf(request.articleId()))
        .userId(Long.valueOf(request.userId()))
        .content(request.content())
        .build();
  }

  public CommentResponse toResponse(Comment comment, String userNickname, boolean likedByMe) {
    return new CommentResponse(
        comment.getId(),
        comment.getArticleId(),
        comment.getUserId(),
        userNickname,
        comment.getContent(),
        comment.getLikeCount(),
        likedByMe,
        comment.getCreatedAt()
    );
  }

  public CommentListResponse toListResponse(List<CommentResponse> comments, String nextCursor,
      String nextAfter, Integer size, Long totalElements, boolean hasNext) {
    return new CommentListResponse(comments, nextCursor, nextAfter, size, totalElements, hasNext);
  }

  public CommentLike toCommentLike(Long commentId, Long userId) {
    return CommentLike.builder()
        .commentId(commentId)
        .likedUserId(userId)
        .likedAt(Instant.now())
        .build();
  }

  public CommentLikeResponse toCommentLikeResponse(Comment comment, String userNickname) {
    return new CommentLikeResponse(
        comment.getId(),
        comment.getId(),
        comment.getArticleId(),
        comment.getUserId(),
        userNickname,
        comment.getContent(),
        comment.getLikeCount(),
        comment.getCreatedAt()
    );
  }
}

