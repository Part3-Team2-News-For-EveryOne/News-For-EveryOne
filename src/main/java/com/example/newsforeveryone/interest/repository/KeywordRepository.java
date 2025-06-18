package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Keyword;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  @Query(value = """
      SELECT *
      FROM keyword
      ORDER BY similarity(name, :keywordName) DESC
      LIMIT 1
      """, nativeQuery = true)
  Optional<Keyword> findMostSimilarityKeyword(@Param("keywordName") String keywordName);

  @Query("SELECT k.id FROM Keyword k")
  List<Long> findAllIds();

  Set<Keyword> findByIdIn(List<Long> ids);

}
