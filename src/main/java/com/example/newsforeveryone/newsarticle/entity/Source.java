package com.example.newsforeveryone.newsarticle.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "source")
@Entity
public class Source extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq_gen")
  @SequenceGenerator(name = "source_seq_gen", sequenceName = "source_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "name", length = 100, unique = true, nullable = false)
  private String name;

  @Column(name = "feed_url", length = 512, unique = true, nullable = false)
  private String feedUrl;

}
