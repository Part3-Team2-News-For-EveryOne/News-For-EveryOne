package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.QInterest;
import com.example.newsforeveryone.interest.entity.QInterestKeyword;
import com.example.newsforeveryone.interest.entity.QKeyword;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

        List<Interest> interests = queryFactory
                .selectDistinct(interest)
                .from(interestKeyword)
                .join(interestKeyword.interest, interest)
                .join(interestKeyword.keyword, keyword)
                .where(interest.name.containsIgnoreCase(word)
                                .or(keyword.name.containsIgnoreCase(word))
//                        .or() // 어디 커서를 봐야되지? 아 ㄹㅇ 잠시만..
//                        .or()
                )
                .orderBy(getPrimaryOrder(orderBy, direction, interest),
                        getSecondaryOrder(direction, interest))
                .limit(limit)
                .fetch();

        List<Long> distinctInterestIds = interests.stream()
                .map(Interest::getId)
                .distinct()
                .toList();

        List<Tuple> result = queryFactory
                .select(interest, keyword.name)
                .from(interestKeyword)
                .join(interestKeyword.interest, interest)
                .join(interestKeyword.keyword, keyword)
                .where(interest.id.in(distinctInterestIds))
                .fetch();

        return result.stream()
                .map(tuple -> Map.entry(
                        Optional.ofNullable(tuple.get(interest)),
                        Objects.requireNonNull(tuple.get(keyword.name))
                ))
                .filter(entry -> entry.getKey().isPresent())
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().get(),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    private OrderSpecifier<?> getPrimaryOrder(String orderBy, String direction, QInterest interest) {
        boolean isDesc = direction.equalsIgnoreCase("desc");

        if (orderBy.equals("name")) {
            if (isDesc) {
                return interest.name.desc();
            }

            return interest.name.asc();
        }

//        if (orderBy.equals("subscriberCount")) {
//            if(isDesc){
//                return
//            }
//        }

        return interest.name.desc();
    }

    private OrderSpecifier<?> getSecondaryOrder(String direction, QInterest interest) {
        boolean isDesc = direction.equalsIgnoreCase("desc");
        if (isDesc) {
            return interest.createdAt.desc();
        }

        return interest.name.asc();
    }

}
