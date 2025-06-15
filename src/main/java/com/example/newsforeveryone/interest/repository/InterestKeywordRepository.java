package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.id.InterestKeywordId;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestKeywordRepository extends
    JpaRepository<InterestKeyword, InterestKeywordId> {

  List<InterestKeyword> findByInterest_Id(Long interestId);

  @Query("""
       SELECT ik
       FROM InterestKeyword ik
       JOIN FETCH ik.keyword
      """)
  List<InterestKeyword> findAllWithKeyword();

  @Query("""
      SELECT DISTINCT ik
      FROM InterestKeyword ik
      INNER JOIN FETCH ik.interest
      INNER JOIN FETCH ik.keyword
      WHERE ik.interest IN :interests
        """)
  List<InterestKeyword> findKeywordsByInterests(@Param("interests") List<Interest> interests);

  @Query("""
      SELECT COUNT (DISTINCT ik.interest)
      FROM InterestKeyword ik
      WHERE ik.interest.name LIKE CONCAT('%', :searchWord, '%')
               OR ik.keyword.name LIKE CONCAT('%', :searchWord, '%')
        """)
  long countInterestAndKeywordsBySearchWord(@Param("searchWord") String searchWord);

  void deleteByInterest_Id(Long interestId);

}
