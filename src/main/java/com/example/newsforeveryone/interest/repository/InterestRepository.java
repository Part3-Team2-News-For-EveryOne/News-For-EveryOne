package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

  @Query(value = """
      SELECT COALESCE(MAX(similarity(i.name, :interest)), 0)
      FROM Interest i
      """, nativeQuery = true)
  Double findMaxSimilarity(@Param("interest") String interest);

  @Query("""
          SELECT COUNT(DISTINCT i)
          FROM Interest i
          LEFT JOIN InterestKeyword ik ON ik.interest = i
          LEFT JOIN Keyword k ON ik.keyword = k
          WHERE i.name LIKE CONCAT('%', :searchWord, '%')
             OR k.name LIKE CONCAT('%', :searchWord, '%')
      """)
  long countInterestsBySearchWord(@Param("searchWord") String searchWord);

}
