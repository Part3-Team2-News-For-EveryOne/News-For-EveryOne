package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class InterestKeywordCustomImpl implements InterestKeywordCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long countSearchInterest(
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

    @Override
    public List<Interest> searchInterestByWordUsingCursor(
            String word,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit
    ) {
        QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;
        QInterest interest = QInterest.interest;
        QKeyword keyword = QKeyword.keyword;

        List<Interest> keywordMatchedInterests = getInterestsInKeyword(word, interestKeyword, keyword);

        return queryFactory
                .select(interest)
                .from(interest)
                .where((interest.in(keywordMatchedInterests).or(interest.name.containsIgnoreCase(word)))
                        .and(cursorCondition(cursor, after, orderBy, direction, interest)))
                .orderBy(getPrimaryOrder(orderBy, direction, interest),
                        getSecondaryOrder(direction, interest))
                .limit(limit + 1)
                .fetch();
    }

    @Override
    public Map<Interest, List<String>> groupKeywordsByInterest(List<Interest> InterestsWithWord) {
        if (InterestsWithWord == null) {
            return new LinkedHashMap<>();
        }

        QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;
        QInterest interest = QInterest.interest;
        QKeyword keyword = QKeyword.keyword;

        List<Tuple> result = queryFactory
                .select(interest, keyword.name)
                .from(interestKeyword)
                .innerJoin(interestKeyword.interest, interest)
                .innerJoin(interestKeyword.keyword, keyword)
                .where(interest.in(InterestsWithWord))
                .fetch();

        return groupInterests(InterestsWithWord, interest, keyword, result);
    }

    private Map<Interest, List<String>> groupInterests(List<Interest> InterestsWithWord, QInterest interest, QKeyword keyword, List<Tuple> result) {
        Map<Interest, List<String>> interestKeywordMap = new LinkedHashMap<>();
        for (Interest interestWithWord : InterestsWithWord) {
            interestKeywordMap.put(interestWithWord, new ArrayList<>());
        }
        for (Tuple tuple : result) {
            Interest interest1 = tuple.get(interest);
            String keywordName = tuple.get(keyword.name);
            if (interest1 != null && keywordName != null) {
                interestKeywordMap.get(interest1).add(keywordName);
            }
        }
        return interestKeywordMap;
    }

    private List<Interest> getInterestsInKeyword(String word, QInterestKeyword interestKeyword, QKeyword keyword) {
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

    private BooleanBuilder cursorCondition(String cursor, String after, String orderBy, String direction, QInterest interest) {
        boolean isAsc = isAsc(direction);
        Instant afterCreatedAt = getAfter(after, isAsc);
        BooleanBuilder whereClause = new BooleanBuilder();

        if (cursor != null) {
            if (isAsc) {
                if (orderBy.equals("subscriberCount")) {
                    return whereClause.andAnyOf(
                            interest.subscriberCount.gt(Integer.valueOf(cursor)),
                            interest.subscriberCount.eq(Integer.valueOf(cursor)).and(interest.createdAt.gt(afterCreatedAt))
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
                            interest.subscriberCount.eq(Integer.valueOf(cursor)).and(interest.createdAt.lt(afterCreatedAt))
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
