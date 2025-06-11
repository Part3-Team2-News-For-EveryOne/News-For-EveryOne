package com.example.newsforeveryone.comment.repository;

import com.example.newsforeveryone.comment.entity.Comment;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

  //  커서 조건 적용
  @Query("""
    SELECT c FROM Comment c 
    WHERE c.articleId = :articleId 
      AND c.deletedAt IS NULL 
      AND (
        :cursor IS NULL 
        OR (
          (:orderBy = 'createdAt' AND 
            ( (:direction = 'ASC' AND c.createdAt > :cursor) 
              OR 
              (:direction = 'DESC' AND c.createdAt < :cursor) ))
          OR 
          (:orderBy = 'likeCount' AND 
            ( (:direction = 'ASC' AND 
                ( (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) > :likeCountCursor 
                  OR 
                  ( (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) = :likeCountCursor 
                    AND c.id > :idCursor )))
              OR 
              (:direction = 'DESC' AND 
                ( (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) < :likeCountCursor 
                  OR 
                  ( (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) = :likeCountCursor 
                    AND c.id < :idCursor ))) ))
        )
      )
    ORDER BY 
      CASE WHEN :orderBy = 'createdAt' AND :direction = 'ASC' THEN c.createdAt END ASC,
      CASE WHEN :orderBy = 'createdAt' AND :direction = 'DESC' THEN c.createdAt END DESC,
      CASE WHEN :orderBy = 'likeCount' AND :direction = 'ASC' THEN (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) END ASC,
      CASE WHEN :orderBy = 'likeCount' AND :direction = 'DESC' THEN (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) END DESC,
      c.id ASC
  """)
  List<Comment> findCommentsWithCursor(
      @Param("articleId") Long articleId,
      @Param("orderBy") String orderBy,
      @Param("direction") String direction,
      @Param("cursor") Instant cursor,
      @Param("likeCountCursor") Long likeCountCursor,
      @Param("idCursor") Long idCursor,
      Pageable pageable
  );

  //  첫 번째 페이지용
  @Query("SELECT c FROM Comment c " +
      "WHERE c.articleId = :articleId AND c.deletedAt IS NULL " +
      "ORDER BY " +
      "CASE WHEN :orderBy = 'createdAt' AND :direction = 'ASC' THEN c.createdAt END ASC, " +
      "CASE WHEN :orderBy = 'createdAt' AND :direction = 'DESC' THEN c.createdAt END DESC, " +
      "CASE WHEN :orderBy = 'likeCount' AND :direction = 'ASC' THEN (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) END ASC, " +
      "CASE WHEN :orderBy = 'likeCount' AND :direction = 'DESC' THEN (SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = c.id) END DESC, " +
      "c.id ASC")
  List<Comment> findCommentsWithoutCursor(
      @Param("articleId") Long articleId,
      @Param("orderBy") String orderBy,
      @Param("direction") String direction,
      Pageable pageable
  );

  // 좋아요 수와 함께 댓글 조회 (N+1 문제 해결)
  @Query("SELECT c FROM Comment c " +
      "LEFT JOIN FETCH c.likes " +
      "WHERE c.articleId = :articleId AND c.deletedAt IS NULL " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findCommentsWithLikes(@Param("articleId") Long articleId);
}