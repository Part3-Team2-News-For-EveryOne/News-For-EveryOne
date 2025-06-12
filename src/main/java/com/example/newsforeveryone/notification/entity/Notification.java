package com.example.newsforeveryone.notification.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq_gen")
  @SequenceGenerator(name = "notification_seq_gen", sequenceName = "notification_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "resource_type", nullable = false, length = 50)
  @Enumerated(value = EnumType.STRING)
  private ResourceType resourceType;

  @Column(name = "resource_id", nullable = false)
  private Long resourceId;

  @Column(name = "confirmed", nullable = false)
  private Boolean confirmed;

  public static Notification ofInterest(Long userId, Long resourceId, String interestName,
      long count) {
    return new Notification(userId, ResourceType.ofInterestContent(interestName, count),
        ResourceType.INTEREST, resourceId);
  }

  public static Notification ofComment(Long authorId, Long resourceId, String likerName) {
    return new Notification(authorId, ResourceType.ofCommentContent(likerName),
        ResourceType.COMMENT, resourceId);
  }

  private Notification(Long userId, String content, ResourceType resourceType, Long resourceId) {
    this.userId = userId;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.confirmed = false;
    this.content = content;
  }

  public void confirmNotification() {
    this.confirmed = true;
  }

}
