package com.example.newsforeveryone.notification.service.impl;

import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.notification.entity.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactoryService {

  public List<Notification> createNotifications(Map<Long, List<Long>> interestToArticle,
      Set<Subscription> subscriptions) {

    List<Notification> notifications = new ArrayList<>();
    for (Subscription subscription : subscriptions) {
      Notification notification = Notification.ofInterest(subscription.getId().getUserId(),
          subscription.getInterest().getId(),
          subscription.getInterest().getName(),
          interestToArticle.get(subscription.getInterest().getId()).size());
      notifications.add(notification);
    }

    return notifications;
  }

}
