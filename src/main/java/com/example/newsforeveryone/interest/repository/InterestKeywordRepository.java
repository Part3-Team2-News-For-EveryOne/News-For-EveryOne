package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.InterestKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, InterestKeywordId> {
}
