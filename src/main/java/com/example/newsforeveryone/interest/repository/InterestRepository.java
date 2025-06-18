package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.repository.querydsl.InterestCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long>, InterestCustom {

  @Query(value = """
      SELECT *
      FROM interest
      ORDER BY similarity(name, :interestName) DESC
      LIMIT 1
      """, nativeQuery = true)
  Optional<Interest> findMostSimilarInterest(@Param("interestName") String interestName);

}
