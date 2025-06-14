package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.QInterest;
import com.example.newsforeveryone.interest.entity.QInterestKeyword;
import com.example.newsforeveryone.interest.entity.QKeyword;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterestKeywordCustomImpl implements InterestKeywordCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Interest> searchInterestByWordUsingCursor(
      String word,
      String orderBy,
      String direction,
      String cursor,
      String after,
      Integer limit
  ) {
    // TODO: 6/14/25  분리 필요
    QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;
    QInterest interest = QInterest.interest;
    QKeyword keyword = QKeyword.keyword;

    List<Interest> keywordMatchedInterests = getInterestsInKeyword(word, interestKeyword, keyword);
    List<Interest> interests = queryFactory
        .select(interest)
        .from(interest)
        .where((interest.in(keywordMatchedInterests).or(interest.name.containsIgnoreCase(word)))
            .and(cursorCondition(cursor, after, orderBy, direction, interest)))
        .orderBy(getPrimaryOrder(orderBy, direction, interest),
            getSecondaryOrder(direction, interest))
        .limit(limit + 1)
        .fetch();

    boolean hasNext = interests.size() > limit;
    List<Interest> slicedInterests = getSlicedInterest(interests, hasNext, limit);
    return new SliceImpl<>(slicedInterests, PageRequest.of(0, limit), hasNext);
  }

  // TODO: 6/14/25 기본 JPA로 분할 필요
  @Override
  public Long countByInterestWord(
      String word,
      List<Interest> keywordMatchedInterests
  ) {
    QInterest interest = QInterest.interest;

    return Objects.requireNonNullElse(
        queryFactory
            .select(interest.count())
            .from(interest)
            .where(interest.in(keywordMatchedInterests).or(interest.name.containsIgnoreCase(word)))
            .fetchOne(), 0L
    );
  }

  private List<Interest> getSlicedInterest(List<Interest> interests, boolean hasNext, int limit) {
    if (hasNext) {
      return interests.subList(0, limit);
    }
    return interests;
  }

  @Override
  public Map<Interest, List<Keyword>> groupKeywordsByUserInterests(List<Interest> interests) {
    if (interests == null) {
      return new HashMap<>();
    }

    QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;
    QInterest interest = QInterest.interest;
    QKeyword keyword = QKeyword.keyword;

    List<Tuple> result = queryFactory
        .select(interest, keyword)
        .from(interestKeyword)
        .innerJoin(interestKeyword.interest, interest)
        .innerJoin(interestKeyword.keyword, keyword)
        .where(interest.in(interests))
        .fetch();

    return groupInterests(interests, interest, keyword, result);
  }

  private Map<Interest, List<Keyword>> groupInterests(List<Interest> InterestsWithWord,
      QInterest interest, QKeyword keyword, List<Tuple> result) {

    Map<Interest, List<Keyword>> interestKeywordMap = new HashMap<>();
    for (Interest interestWithWord : InterestsWithWord) {
      interestKeywordMap.put(interestWithWord, new ArrayList<>());
    }
    for (Tuple tuple : result) {
      Interest interestTuple = tuple.get(interest);
      Keyword keywordTuple = tuple.get(keyword);
      if (interestTuple != null && keywordTuple != null) {
        interestKeywordMap.get(interestTuple).add(keywordTuple);
      }
    }

    return interestKeywordMap;
  }

  private List<Interest> getInterestsInKeyword(String word, QInterestKeyword interestKeyword,
      QKeyword keyword) {
    List<Keyword> keywordIds = queryFactory
        .select(keyword)
        .from(keyword)
        .where(keyword.name.containsIgnoreCase(word))
        .fetch();

    return queryFactory
        .selectDistinct(interestKeyword.interest)
        .from(interestKeyword)
        .where(interestKeyword.keyword.in(keywordIds))
        .fetch();
  }

  // TODO: 6/14/25 분기 단순화 필요
  private BooleanBuilder cursorCondition(String cursor, String after, String orderBy,
      String direction, QInterest interest) {
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

  private OrderSpecifier<?> getPrimaryOrder(String orderBy, String direction, QInterest interest) {
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

  private OrderSpecifier<?> getSecondaryOrder(String direction, QInterest interest) {
    boolean isAsc = isAsc(direction);
    if (isAsc) {
      return interest.createdAt.asc();
    }

    return interest.createdAt.desc();
  }

}
