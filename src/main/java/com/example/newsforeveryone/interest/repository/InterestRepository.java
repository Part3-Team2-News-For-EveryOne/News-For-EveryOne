package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    @Query(value = """
            SELECT MAX(similarity(i.name, :interest))
            FROM interest i
            """, nativeQuery = true)
    Optional<Double> findMaxSimilarity(@Param("interest") String interest);

}
