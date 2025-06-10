package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query(value = """
            SELECT *, similarity(i.name, :keyword) AS score
            FROM keyword i
            where similarity(i.name, :keyword) > :threshold
            ORDER BY score desc 
            LIMIT 1
            """, nativeQuery = true)
    Optional<Keyword> findMaxSimilarityKeyword(@Param("keyword") String keyword, @Param("threshold") double threshold);

}
