package com.example.newsforeveryone.comment.controller;

import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.dto.CommentUpdateRequest;
import com.example.newsforeveryone.comment.service.CommentService;
import com.example.newsforeveryone.user.config.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentController {

  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<CommentListResponse> getComments(
      @RequestParam(required = false) Long articleId,
      @RequestParam(defaultValue = "createdAt") String orderBy,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Long after,
      @RequestParam(defaultValue = "50") Integer limit) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    CommentListResponse response = commentService.getComments(
        articleId, orderBy, direction, cursor, after, limit, userDetails.getUserId());
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CommentCreateRequest request) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    CommentResponse response = commentService.createComment(request, userDetails.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeResponse> likeComment(
      @PathVariable Long commentId) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    CommentLikeResponse response = commentService.likeComment(commentId, userDetails.getUserId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public ResponseEntity<Void> unlikeComment(
      @PathVariable Long commentId) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    commentService.unlikeComment(commentId, userDetails.getUserId());
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable Long commentId,
      @Valid @RequestBody CommentUpdateRequest request) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    CommentResponse response = commentService.updateComment(commentId, request, userDetails.getUserId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> softDeleteComment(
      @PathVariable Long commentId) {

    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    commentService.softDeleteComment(commentId, userDetails.getUserId());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(@PathVariable Long commentId) {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    commentService.hardDeleteComment(commentId, userDetails.getUserId());
    return ResponseEntity.noContent().build();
  }
}