package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.FakeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FakeCommentRepository extends JpaRepository<FakeComment, Long> {
  default long countByArticleIdAndDeletedAtIsNull(Long articleId) {
    return 15L;
  }
}
