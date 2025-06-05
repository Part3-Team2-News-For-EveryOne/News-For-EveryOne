package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.InterestKeywordId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, InterestKeywordId> {
  @Query("""
     SELECT ik
     FROM InterestKeyword ik
     JOIN FETCH ik.keyword
    """)
  List<InterestKeyword> findAllWithKeyword();
}
