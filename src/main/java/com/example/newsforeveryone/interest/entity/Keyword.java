package com.example.newsforeveryone.interest.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "keyword_seq_gen")
  @SequenceGenerator(name = "keyword_seq_gen", sequenceName = "keyword_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  public Keyword(String name) {
    this.name = name;
  }

}
