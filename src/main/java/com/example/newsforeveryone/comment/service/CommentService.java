package com.example.newsforeveryone.comment.service;

import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.dto.CommentUpdateRequest;

public interface CommentService {
  CommentListResponse getComments(
      Long articleId,
      String orderBy,
      String direction,
      String cursor,
      Long after,
      Integer limit,
      Long requestUserId
  );

  CommentResponse createComment(CommentCreateRequest request, Long requestUserId);

  CommentLikeResponse likeComment(Long commentId, Long requestUserId);

  void unlikeComment(Long commentId, Long requestUserId);

  CommentResponse updateComment(Long commentId, CommentUpdateRequest request, Long requestUserId);

  void softDeleteComment(Long commentId, Long requestUserId);

  void hardDeleteComment(Long commentId, Long requestUserId);
}