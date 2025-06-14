package com.example.newsforeveryone.interest.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

  @EmbeddedId
  private SubscriptionId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("interestId")
  @JoinColumn(name = "interest_id", nullable = false)
  private Interest interest;

  @CreatedDate
  @Column(name = "subscribed_at", nullable = false)
  private Instant subscribeAt;

  public Subscription(Interest interest, Long userId) {
    this.interest = interest;
    this.id = new SubscriptionId(interest.getId(), userId);
  }

}
