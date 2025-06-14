package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.id.InterestKeywordId;
import com.example.newsforeveryone.interest.repository.querydsl.InterestKeywordCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestKeywordRepository extends
    JpaRepository<InterestKeyword, InterestKeywordId>, InterestKeywordCustom {

  List<InterestKeyword> findByInterest_Id(Long interestId);

  @Query("""
       SELECT ik
       FROM InterestKeyword ik
       JOIN FETCH ik.keyword
      """)
  List<InterestKeyword> findAllWithKeyword();


  @Query("""
    SELECT ik
    FROM InterestKeyword ik
    INNER JOIN FETCH ik.interest
    INNER JOIN FETCH ik.keyword
    WHERE ik.interest IN :interests
      """)
  List<InterestKeyword> groupKeywordsByInterests(@Param("interests") List<Interest> interests);

  void deleteByInterest_Id(Long interestId);

}
