package com.example.newsforeveryone.notification.repository;

import com.example.newsforeveryone.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query("""
      SELECT n From Notification as n
      WHERE n.createdAt > :createdAt
         AND n.userId = :userId
         AND n.confirmed = false 
      ORDER BY n.createdAt ASC
         """)
  Page<Notification> findAllByUserIdWithCursorAsc(@Param("userId") Long userId,
      @Param("createdAt") Instant createdAt, Pageable pageable);

  List<Notification> findAllByUserIdAndConfirmed(Long userId, boolean isConfirmed);

  void deleteAllByConfirmedTrue();

}
