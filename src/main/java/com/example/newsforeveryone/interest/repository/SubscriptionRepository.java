package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

  @Query("SELECT DISTINCT s FROM Subscription s JOIN FETCH s.interest WHERE s.interest.id IN :interestIds")
  Set<Subscription> findAllWithInterestByInterestIdIn(@Param("interestIds") Set<Long> interestIds);

  void deleteByInterest_Id(Long interestId);

}
