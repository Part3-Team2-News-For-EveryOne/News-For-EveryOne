package com.example.newsforeveryone.comment.service.impl;

import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.dto.CommentUpdateRequest;
import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.comment.entity.CommentLike;
import com.example.newsforeveryone.comment.mapper.CommentMapper;
import com.example.newsforeveryone.comment.repository.CommentLikeRepository;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.comment.service.CommentService;
import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final CommentMapper commentMapper;
  private final UserRepository userRepository;

  @Override
  public CommentListResponse getComments(
      Long articleId, String orderBy, String direction,
      String cursor, Long after, Integer limit, Long requestUserId) {

    CommentQueryParams params = buildQueryParams(articleId, orderBy, cursor, after, limit, requestUserId, direction);
    List<Comment> comments = fetchComments(params);
    boolean hasNext = comments.size() > limit;
    if (hasNext) comments = comments.subList(0, limit);
    if (comments.isEmpty()) {
      return commentMapper.toListResponse(Collections.emptyList(), null, null, limit, 0L, false);
    }

    // 1. 댓글 작성자 ID 목록 추출
    Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());

    // 2. ID 목록으로 사용자 정보 한번에 조회
    Map<Long, String> userNicknameMap = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, User::getNickname));

    // 3. 현재 사용자가 좋아요 누른 댓글 ID 목록 조회
    Set<Long> likedIds = fetchLikedIds(comments, params.requestUserIdLong());

    // 4. Map을 활용하여 DTO 변환 (DB 추가 조회 없음)
    List<CommentResponse> responses = comments.stream()
        .map(comment -> {
          String nickname = userNicknameMap.getOrDefault(comment.getUserId(), "탈퇴한 사용자");
          boolean likedByMe = likedIds.contains(comment.getId());
          return commentMapper.toResponse(comment, nickname, likedByMe);
        })
        .toList();

    String nextCursor = hasNext ? comments.get(comments.size() - 1).getCreatedAt().toString() : null;
    String nextAfter = hasNext ? comments.get(comments.size() - 1).getId().toString() : null;
    Long totalElements = getTotalCommentCount(articleId);

    return commentMapper.toListResponse(responses, nextCursor, nextAfter, limit, totalElements, hasNext);
  }

  @Override
  @Transactional
  public CommentResponse createComment(CommentCreateRequest req, Long requestUserId) {
    try {
      Long reqUserId = Long.parseLong(req.userId());
      validateUser(reqUserId, requestUserId);

      Comment entity = commentMapper.toEntity(req);
      Comment saved = commentRepository.save(entity);
      String nickname = getUserNickname(saved.getUserId());
      return commentMapper.toResponse(saved, nickname, false);

    } catch (NumberFormatException e) {
      throw new BaseException(ErrorCode.UNAUTHORIZED_USER_ACCESS);
    }
  }

  @Override
  @Transactional
  public CommentLikeResponse likeComment(Long commentId, Long requestUserId) {
    Comment comment = getCommentOrThrow(commentId);
    if (commentLikeRepository.existsByCommentIdAndLikedUserId(commentId, requestUserId))
      throw new BaseException(ErrorCode.COMMENT_LIKE_DUPLICATED);

    CommentLike like = commentMapper.toCommentLike(commentId, requestUserId);
    comment.addLike(like);

    String nickname = getUserNickname(comment.getUserId());
    return commentMapper.toCommentLikeResponse(comment, nickname);
  }


  @Override
  @Transactional
  public void unlikeComment(Long commentId, Long requestUserId) {
    Comment comment = getCommentOrThrow(commentId);
    CommentLike like = commentLikeRepository.findByCommentIdAndLikedUserId(commentId, requestUserId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

    commentLikeRepository.delete(like);
    comment.decreaseLikeCount();
  }

  @Override
  @Transactional
  public CommentResponse updateComment(Long commentId, CommentUpdateRequest req, Long requestUserId) {
    Comment comment = getCommentForUpdateOrDelete(commentId);
    if (comment.getDeletedAt() != null)
      throw new BaseException(ErrorCode.COMMENT_ALREADY_DELETED);
    if (!comment.getUserId().equals(requestUserId))
      throw new BaseException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);

    comment.updateContent(req.content());
    Comment updated = commentRepository.save(comment);
    boolean likedByMe = commentLikeRepository.existsByCommentIdAndLikedUserId(commentId, requestUserId);
    String nickname = getUserNickname(updated.getUserId());
    return commentMapper.toResponse(updated, nickname, likedByMe);
  }

  @Override
  @Transactional
  public void softDeleteComment(Long commentId, Long requestUserId) {
    Comment comment = getCommentForUpdateOrDelete(commentId);
    if (comment.getDeletedAt() != null)
      throw new BaseException(ErrorCode.COMMENT_ALREADY_DELETED);
    if (!comment.getUserId().equals(requestUserId))
      throw new BaseException(ErrorCode.COMMENT_DELETE_FORBIDDEN);

    comment.setDeletedAt(Instant.now());
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void hardDeleteComment(Long commentId, Long requestUserId) {
    Comment comment = getCommentOrThrow(commentId);
    if (!comment.getUserId().equals(requestUserId))
      throw new BaseException(ErrorCode.COMMENT_DELETE_FORBIDDEN);

    commentLikeRepository.deleteByCommentIdAndLikedUserId(commentId, requestUserId);
    commentRepository.delete(comment);
  }

  // 내부 메서드

  private CommentQueryParams buildQueryParams(
      Long articleId, String orderBy, String cursor, Long after,
      Integer limit, Long userId, String direction) {
    return new CommentQueryParams(
        articleId,
        userId,
        PageRequest.of(0, limit + 1),
        cursor != null ? Instant.parse(cursor) : null,
        after,
        orderBy,
        direction
    );
  }

  private Long getTotalCommentCount(Long articleId) {
    return commentRepository.countByArticleIdAndDeletedAtIsNull(articleId);
  }

  private List<Comment> fetchComments(CommentQueryParams p) {
    if (p.cursorTime() != null || p.idCursor() != null) {
      Long likeCountCursor = null;
      if ("likeCount".equals(p.orderBy()) && p.idCursor() != null) {
        Comment cursorComment = getCommentForCursor(p.idCursor());
        likeCountCursor = cursorComment.getLikeCount();
      }
      return commentRepository.findCommentsWithCursor(
          p.articleIdLong(), p.orderBy(), p.direction(), p.cursorTime(), likeCountCursor, p.idCursor(), p.pageable());
    }
    return commentRepository.findCommentsWithoutCursor(
        p.articleIdLong(), p.orderBy(), p.direction(), p.pageable());
  }

  private Set<Long> fetchLikedIds(List<Comment> comments, Long userId) {
    if (userId == null || comments.isEmpty()) return Set.of();
    List<Long> ids = comments.stream().map(Comment::getId).toList();
    return new HashSet<>(
        commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, ids));
  }

  private void validateUser(Long reqUser, Long actualUser) {
    if (!reqUser.equals(actualUser)) throw new BaseException(ErrorCode.UNAUTHORIZED_USER_ACCESS);
  }

  // 소프트 삭제 여부와 무관하게 댓글 조회 (update/delete 시에는 반드시 존재만 확인)
  private Comment getCommentForUpdateOrDelete(Long id) {
    return commentRepository.findById(id)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
  }

  // deletedAt이 null인 댓글만 조회
  private Comment getCommentOrThrow(Long id) {
    return commentRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
  }

  // 커서용: 삭제 여부 무관하게 조회
  private Comment getCommentForCursor(Long id) {
    return commentRepository.findById(id)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private String getUserNickname(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    return user.getNickname();
  }

  private record CommentQueryParams(
      Long articleIdLong,
      Long requestUserIdLong,
      Pageable pageable,
      Instant cursorTime,
      Long idCursor,
      String orderBy,
      String direction
  ) {}

}
