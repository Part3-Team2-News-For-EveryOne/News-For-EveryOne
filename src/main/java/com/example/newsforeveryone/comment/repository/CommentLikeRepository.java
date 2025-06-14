package com.example.newsforeveryone.comment.repository;

import com.example.newsforeveryone.comment.entity.CommentLike;
import com.example.newsforeveryone.comment.entity.id.CommentLikeId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

  Optional<CommentLike> findByCommentIdAndLikedUserId(Long commentId, Long userId);

  boolean existsByCommentIdAndLikedUserId(Long commentId, Long userId);

  void deleteByCommentIdAndLikedUserId(Long commentId, Long userId);

  // 특정 댓글의 좋아요 수 조회
  @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = :commentId")
  long countByCommentId(@Param("commentId") Long commentId);

  // 특정 사용자가 좋아요한 댓글 ID 목록 조회
  @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.likedUserId = :userId AND cl.commentId IN :commentIds")
  List<Long> findLikedCommentIdsByUserIdAndCommentIds(@Param("userId") Long userId,
      @Param("commentIds") List<Long> commentIds);
}
