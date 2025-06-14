package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.QInterest;
import com.example.newsforeveryone.interest.entity.QInterestKeyword;
import com.example.newsforeveryone.interest.entity.QKeyword;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterestCustomImpl implements InterestCustom {

  private static final QInterest interest = QInterest.interest;
  private static final QKeyword keyword = QKeyword.keyword;
  private static final QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Interest> searchInterestByWordWithCursor(
      String searchWord,
      String orderBy,
      String direction,
      String cursor,
      String after,
      Integer limit
  ) {
    List<Interest> interests = queryFactory
        .select(interest)
        .from(interest)
        .where(
            interestMatchesKeywordOrName(searchWord)
            .and(cursorCondition(cursor, after, orderBy, direction))
        )
        .orderBy(
            getPrimaryOrder(orderBy, direction),
            getSecondaryOrder(direction)
        )
        .limit(limit + 1)
        .fetch();

    boolean hasNext = interests.size() > limit;
    List<Interest> slicedInterests = getSlicedInterest(interests, hasNext, limit);
    return new SliceImpl<>(slicedInterests, PageRequest.of(0, limit), hasNext);
  }

  private BooleanExpression interestMatchesKeywordOrName(String word) {
    return interest.in(
        JPAExpressions
            .select(interestKeyword.interest)
            .from(interestKeyword)
            .join(interestKeyword.keyword, keyword)
            .where(keyword.name.containsIgnoreCase(word))
    ).or(
        interest.name.containsIgnoreCase(word)
    );
  }

  private List<Interest> getSlicedInterest(List<Interest> interests, boolean hasNext, int limit) {
    if (hasNext) {
      return interests.subList(0, limit);
    }
    return interests;
  }

  // TODO: 6/14/25 분기 단순화 필요
  private BooleanBuilder cursorCondition(String cursor, String after, String orderBy,
      String direction) {
    boolean isAsc = isAsc(direction);
    Instant afterCreatedAt = getAfter(after, isAsc);
    BooleanBuilder whereClause = new BooleanBuilder();

    if (cursor != null) {
      if (isAsc) {
        if (orderBy.equals("subscriberCount")) {
          return whereClause.andAnyOf(
              interest.subscriberCount.gt(Integer.valueOf(cursor)),
              interest.subscriberCount.eq(Integer.valueOf(cursor))
                  .and(interest.createdAt.gt(afterCreatedAt))
          );
        } else {
          return whereClause.andAnyOf(
              interest.name.gt(cursor)
          );
        }
      } else {
        if (orderBy.equals("subscriberCount")) {
          return whereClause.andAnyOf(
              interest.subscriberCount.lt(Integer.valueOf(cursor)),
              interest.subscriberCount.eq(Integer.valueOf(cursor))
                  .and(interest.createdAt.lt(afterCreatedAt))
          );
        } else {
          return whereClause.andAnyOf(
              interest.name.lt(cursor)
          );
        }
      }
    }

    return whereClause;
  }

  private boolean isAsc(String direction) {
    if (direction == null) {
      return false;
    }

    return direction.equalsIgnoreCase("asc");
  }

  private Instant getAfter(String after, boolean isAsc) {
    if (after == null || after.isBlank()) {
      if (isAsc) {
        return Instant.EPOCH;
      }
      return Instant.now();
    }

    return Instant.parse(after);
  }

  private OrderSpecifier<?> getPrimaryOrder(String orderBy, String direction) {
    boolean isAsc = isAsc(direction);
    if (orderBy.equals("subscriberCount")) {
      if (isAsc) {
        return interest.subscriberCount.asc();
      }
      return interest.subscriberCount.desc();
    }

    if (isAsc) {
      return interest.name.asc();
    }
    return interest.name.desc();
  }

  private OrderSpecifier<?> getSecondaryOrder(String direction) {
    boolean isAsc = isAsc(direction);
    if (isAsc) {
      return interest.createdAt.asc();
    }

    return interest.createdAt.desc();
  }

}
