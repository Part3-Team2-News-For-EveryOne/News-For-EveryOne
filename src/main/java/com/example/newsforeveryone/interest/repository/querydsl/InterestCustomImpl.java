package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.QInterest;
import com.example.newsforeveryone.interest.entity.QInterestKeyword;
import com.example.newsforeveryone.interest.entity.QKeyword;
import com.example.newsforeveryone.interest.repository.querydsl.condition.InterestCursorConditionBuilder;
import com.example.newsforeveryone.interest.repository.querydsl.condition.InterestOrderSpecifier;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
  private final InterestOrderSpecifier interestOrderSpecifier;
  private final InterestCursorConditionBuilder interestCursorConditionBuilder;

  @Override
  public Slice<Interest> searchInterestByWordWithCursor(
      String searchWord,
      String orderBy,
      String direction,
      String cursor,
      String after,
      Integer limit
  ) {

    boolean isAsc = isAsc(direction);
    BooleanExpression interestMatchesKeywordOrName = interestMatchesKeywordOrName(searchWord);
    BooleanExpression cursorCondition = interestCursorConditionBuilder.cursorCondition(cursor,
        after, orderBy, isAsc);
    OrderSpecifier<?> primaryOrder = interestOrderSpecifier.getSubscriberCountPrimaryOrder(
        orderBy, isAsc);
    OrderSpecifier<Instant> secondaryOrder = interestOrderSpecifier.getOrderSpecifier(isAsc,
        interest.createdAt);

    List<Interest> interests = queryFactory
        .select(interest)
        .from(interest)
        .where(interestMatchesKeywordOrName.and(cursorCondition))
        .orderBy(primaryOrder, secondaryOrder)
        .limit(limit + 1)
        .fetch();

    return toCustomSlice(limit, interests);
  }

  private SliceImpl<Interest> toCustomSlice(Integer limit, List<Interest> interests) {
    boolean hasNext = interests.size() > limit;
    List<Interest> slicedInterests = getSlicedInterest(interests, hasNext, limit);
    return new SliceImpl<>(slicedInterests, PageRequest.of(0, limit), hasNext);
  }

  private boolean isAsc(String direction) {
    if (direction == null) {
      return false;
    }
    return direction.equalsIgnoreCase("asc");
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

}
