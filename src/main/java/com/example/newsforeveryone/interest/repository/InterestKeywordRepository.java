package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.id.InterestKeywordId;
import com.example.newsforeveryone.interest.repository.querydsl.InterestKeywordCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, InterestKeywordId>, InterestKeywordCustom {

    List<InterestKeyword> findByInterest_Id(Long interestId);

    void deleteByInterest_Id(Long interestId);

}
