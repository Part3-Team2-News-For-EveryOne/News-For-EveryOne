package com.example.newsforeveryone.newsarticle.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comment")
public class FakeComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long articleId;
  private Long userId;
  private String content;
  private Instant deletedAt;
  private Instant createdAt;
  private Instant updatedAt;

}