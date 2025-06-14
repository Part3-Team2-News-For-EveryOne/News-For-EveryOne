package com.example.newsforeveryone.comment.repository;

import com.example.newsforeveryone.comment.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

  // --- 첫 페이지 조회 (커서 없을 때) ---

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId AND c.deletedAt IS NULL
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Comment> findFirstPageByCreatedAtDesc(
      @Param("articleId") Long articleId,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId AND c.deletedAt IS NULL
      ORDER BY c.createdAt ASC, c.id ASC
      """)
  List<Comment> findFirstPageByCreatedAtAsc(
      @Param("articleId") Long articleId,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId AND c.deletedAt IS NULL
      ORDER BY c.likeCount DESC, c.id DESC
      """)
  List<Comment> findFirstPageByLikeCountDesc(
      @Param("articleId") Long articleId,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId AND c.deletedAt IS NULL
      ORDER BY c.likeCount ASC, c.id ASC
      """)
  List<Comment> findFirstPageByLikeCountAsc(
      @Param("articleId") Long articleId,
      Pageable pageable
  );


  // --- 커서 기반 다음 페이지 조회 ---

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId
        AND c.deletedAt IS NULL
        AND c.createdAt < :cursor
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Comment> findNextPageByCreatedAtDesc(
      @Param("articleId") Long articleId,
      @Param("cursor") Instant cursor,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId
        AND c.deletedAt IS NULL
        AND c.createdAt > :cursor
      ORDER BY c.createdAt ASC, c.id ASC
      """)
  List<Comment> findNextPageByCreatedAtAsc(
      @Param("articleId") Long articleId,
      @Param("cursor") Instant cursor,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId
        AND c.deletedAt IS NULL
        AND (c.likeCount < :likeCountCursor OR (c.likeCount = :likeCountCursor AND c.id < :idCursor))
      ORDER BY c.likeCount DESC, c.id DESC
      """)
  List<Comment> findNextPageByLikeCountDesc(
      @Param("articleId") Long articleId,
      @Param("likeCountCursor") Long likeCountCursor,
      @Param("idCursor") Long idCursor,
      Pageable pageable
  );

  @Query("""
      SELECT c FROM Comment c
      WHERE c.articleId = :articleId
        AND c.deletedAt IS NULL
        AND (c.likeCount > :likeCountCursor OR (c.likeCount = :likeCountCursor AND c.id > :idCursor))
      ORDER BY c.likeCount ASC, c.id ASC
      """)
  List<Comment> findNextPageByLikeCountAsc(
      @Param("articleId") Long articleId,
      @Param("likeCountCursor") Long likeCountCursor,
      @Param("idCursor") Long idCursor,
      Pageable pageable
  );

  // 좋아요 수와 함께 댓글 조회 (N+1 문제 해결)
  @Query("SELECT c FROM Comment c " +
      "LEFT JOIN FETCH c.likes " +
      "WHERE c.articleId = :articleId AND c.deletedAt IS NULL " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findCommentsWithLikes(@Param("articleId") Long articleId);

  long countByArticleIdAndDeletedAtIsNull(Long articleId);
}