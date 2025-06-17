package com.example.newsforeveryone.interest.repository.querydsl.condition;

import com.example.newsforeveryone.interest.entity.QInterest;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InterestCursorConditionBuilder {

  private static final QInterest interest = QInterest.interest;

  public BooleanExpression cursorCondition(String cursor, String after, String orderBy,
      boolean isAsc) {
    if (cursor == null) {
      return null;
    }

    if (orderBy.equals("subscriberCount")) {
      return getSubscriberCountCondition(cursor, after, isAsc);
    }

    return getNameCondition(cursor, isAsc);
  }

  private BooleanExpression getSubscriberCountCondition(String cursor, String after,
      boolean isAsc) {
    Instant parsedAfter = parseOrDefaultAfterCursor(after, isAsc);
    Integer parsedCursor = parseOrDefaultCountCursor(cursor, isAsc);
    if (isAsc) {
      return interest.subscriberCount.gt(parsedCursor)
          .or(
              interest.subscriberCount.eq(parsedCursor)
                  .and(interest.createdAt.gt(parsedAfter))
          );
    }
    return interest.subscriberCount.lt(parsedCursor)
        .or(
            interest.subscriberCount.eq(parsedCursor)
                .and(interest.createdAt.lt(parsedAfter))
        );
  }

  private BooleanExpression getNameCondition(String cursor, boolean isAsc) {
    if (isAsc) {
      return interest.name.gt(cursor);
    }
    return interest.name.lt(cursor);
  }

  private Integer parseOrDefaultCountCursor(String cursor, boolean isAsc) {
    try {
      return Integer.valueOf(cursor);
    } catch (NumberFormatException e) {
      if (isAsc) {
        return Integer.MIN_VALUE;
      }
      return Integer.MAX_VALUE;
    }
  }

  private Instant parseOrDefaultAfterCursor(String after, boolean isAsc) {
    try {
      return Instant.parse(after);
    } catch (Exception e) {
      if (isAsc) {
        return Instant.EPOCH;
      }
      return Instant.now();
    }
  }

}
