package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.Source;
import com.example.newsforeveryone.newsarticle.entity.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface SourceRepository extends JpaRepository<Source, Long> {

  List<Source> findAllByType(SourceType type);

  @Query("select s.feedUrl from Source s where s.type = :type")
  Optional<List<String>> findAllFeedUrlByType(@Param("type") SourceType type);

  @Query("SELECT s.feedUrl FROM Source s")
  Optional<List<String>> findAllFeedUrl();

  Optional<Source> findByName(String sourceName);

  @Query("SELECT s.name FROM Source s ORDER BY s.name ASC")
  List<String> findAllNames();
}
