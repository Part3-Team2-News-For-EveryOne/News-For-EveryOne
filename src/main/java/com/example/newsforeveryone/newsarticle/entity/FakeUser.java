package com.example.newsforeveryone.newsarticle.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Table(name = "users")
@Entity
public class FakeUser extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interest_seq_gen")
  @SequenceGenerator(name = "interest_seq_gen", sequenceName = "interest_id_seq", allocationSize = 50)
  private Long id;
}
