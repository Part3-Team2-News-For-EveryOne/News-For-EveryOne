package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.repository.querydsl.InterestCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long>, InterestCustom {

  @Query(value = """
      SELECT COALESCE(MAX(similarity(i.name, :interest)), 0)
      FROM Interest i
      """, nativeQuery = true)
  Double findMaxSimilarity(@Param("interest") String interest);

}
