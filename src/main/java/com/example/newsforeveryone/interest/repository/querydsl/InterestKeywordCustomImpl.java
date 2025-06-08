package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.QInterest;
import com.example.newsforeveryone.interest.entity.QInterestKeyword;
import com.example.newsforeveryone.interest.entity.QKeyword;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InterestKeywordCustomImpl implements InterestKeywordCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Interest, List<String>> searchByWord(
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

        // 키워드에서
        List<Long> keywordIds = queryFactory
                .select(keyword.id)
                .from(keyword)
                .where(keyword.name.containsIgnoreCase(word))
                .fetch();
        // InterestKeyword에서 가져오기
        List<Long> keywordInterestIds = queryFactory
                .selectDistinct(interestKeyword.interest.id)
                .from(interestKeyword)
                .where(interestKeyword.keyword.id.in(keywordIds))
                .fetch();

        // Interest에서 가져오기
        List<Long> targetInterestIds = queryFactory
                .select(interest.id)
                .from(interest)
                .where(interest.id.in(keywordInterestIds)
                        .or(interest.name.containsIgnoreCase(word))
                        .and(cursorCondition(cursor, after, orderBy, direction, interest)))
                .orderBy(
                        getPrimaryOrder(direction, orderBy, interest),
                        getSecondaryOrder(direction, interest)
                )
                .limit(limit)
                .fetch();

        List<Tuple> result = queryFactory
                .select(interest, keyword.name)
                .from(interestKeyword)
                .join(interestKeyword.interest, interest)
                .join(interestKeyword.keyword, keyword)
                .where(interest.id.in(targetInterestIds))
                .fetch();

        return result.stream()
                .filter(tuple -> tuple.get(interest) != null && tuple.get(keyword.name) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(interest),
                        Collectors.mapping(tuple -> tuple.get(keyword.name), Collectors.toList())
                ));
    }

    // null일떄 처리필요
    private BooleanExpression cursorCondition(String cursor, String after, String orderBy, String direction, QInterest interest) {
        if (after != null && !after.isBlank() && cursor != null && !cursor.isBlank()) {

            if (direction.equalsIgnoreCase("asc")) {
                if (orderBy.equals("subscriberCount")) {
                    return interest.subscriberCount.gt(Integer.valueOf(cursor))
                            .and(interest.createdAt.gt(Instant.parse(after)));
                } else {
                    return interest.name.gt(cursor)
                            .and(interest.createdAt.gt(Instant.parse(after)));
                }
            }


            if (orderBy.equals("subscriberCount")) {
                return interest.subscriberCount.lt(Integer.valueOf(cursor))
                        .and(interest.createdAt.lt(Instant.parse(after)));
            }
            return interest.name.lt(cursor)
                    .and(interest.createdAt.lt(Instant.parse(after)));
        }


        return cursor != null ? interest.name.gt(cursor) : null;
    }


    private OrderSpecifier<?> getPrimaryOrder(String orderBy, String direction, QInterest interest) {
        boolean isAsc = direction.equalsIgnoreCase("asc");
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
        boolean isAsc = direction.equalsIgnoreCase("asc");
        if (isAsc) {
            return interest.createdAt.asc();
        }

        return interest.createdAt.desc();
    }

}
