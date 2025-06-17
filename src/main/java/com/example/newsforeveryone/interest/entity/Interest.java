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
@Table(name = "interest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interest_seq_gen")
  @SequenceGenerator(name = "interest_seq_gen", sequenceName = "interest_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "subscriber_count", nullable = false)
  private int subscriberCount;

  public Interest(String name) {
    this.name = name;
  }

  public void increaseSubscriberCount() {
    this.subscriberCount++;
  }

  public void decreaseSubscriberCount() {
    this.subscriberCount--;
  }

}
