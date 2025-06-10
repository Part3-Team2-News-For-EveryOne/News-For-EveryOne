package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {

  @Query("SELECT s.feedUrl FROM Source s")
  Optional<List<String>> findAllFeedUrl();

  Optional<Source> findByName(String sourceName);

  @Query("SELECT s.name FROM Source s ORDER BY s.name ASC")
  List<String> findAllNames();
}
