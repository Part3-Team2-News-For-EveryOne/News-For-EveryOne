package com.example.newsforeveryone.interest.repository.querydsl.condition;

import com.example.newsforeveryone.interest.entity.QInterest;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import org.springframework.stereotype.Component;

@Component
public class InterestOrderSpecifier {

  private static final QInterest interest = QInterest.interest;

  public OrderSpecifier<?> getSubscriberCountPrimaryOrder(String orderBy, boolean isAsc) {
    if (orderBy != null && orderBy.equals("subscriberCount")) {
      return getOrderSpecifier(isAsc, interest.subscriberCount);
    }
    return getOrderSpecifier(isAsc, interest.name);
  }

  public <T extends Comparable<?>> OrderSpecifier<T> getOrderSpecifier(
      boolean isAsc,
      Path<T> path
  ) {
    if (isAsc) {
      return new OrderSpecifier<>(Order.ASC, path);
    }
    return new OrderSpecifier<>(Order.DESC, path);
  }

}
