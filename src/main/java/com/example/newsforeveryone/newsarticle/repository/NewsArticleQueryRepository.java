package com.example.newsforeveryone.newsarticle.repository;

import com.example.newsforeveryone.newsarticle.dto.ArticleDto;
import com.example.newsforeveryone.newsarticle.dto.CursorPageArticleRequest;
import com.example.newsforeveryone.newsarticle.dto.CursorPageResponseArticleDto;
import com.example.newsforeveryone.newsarticle.dto.QArticleDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.example.newsforeveryone.newsarticle.entity.QArticleInterest.articleInterest;
import static com.example.newsforeveryone.newsarticle.entity.QArticleView.articleView;
import static com.example.newsforeveryone.newsarticle.entity.QNewsArticle.newsArticle;
import static com.example.newsforeveryone.newsarticle.entity.QNewsArticleMetric.newsArticleMetric;


@Repository
@RequiredArgsConstructor
public class NewsArticleQueryRepository {

  private final JPAQueryFactory queryFactory;

  public CursorPageResponseArticleDto findArticlePage(CursorPageArticleRequest request,
      Long userId) {

    List<ArticleDto> content = fetchArticles(request, userId);
    Long totalElements = fetchTotalCount(request);


    boolean hasNext = content.size() > request.limit();
    if (hasNext) {
      content.remove(request.limit().intValue());
    }

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !content.isEmpty()) {
      ArticleDto lastArticle = content.get(content.size() - 1);
      String orderBy = request.getOrderByWithDefault();

      nextAfter = lastArticle.publishDate();

      nextCursor = switch (orderBy) {
        case "commentcount" -> lastArticle.commentCount().toString();
        case "viewcount" -> lastArticle.viewCount().toString();
        default -> lastArticle.publishDate().toString();
      };
    }

    return new CursorPageResponseArticleDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  private Long fetchTotalCount(CursorPageArticleRequest request) {
    return queryFactory
        .select(newsArticle.id.count())
        .from(newsArticle)
        .where(
            newsArticle.deletedAt.isNull(),
            hasKeyword(request.keyword()),
            interestIdEquals(request.interestId()),
            publishDateBetween(request.publishDateFrom(), request.publishDateTo()),
            sourceIn(request.sourceIn())
        )
        .fetchOne();
  }


  private List<ArticleDto> fetchArticles(CursorPageArticleRequest request, Long userId) {
    return queryFactory
        .select(new QArticleDto(
            newsArticle.id,
            newsArticle.sourceName,
            newsArticle.link,
            newsArticle.title,
            newsArticle.publishedAt,
            newsArticle.summary,
            newsArticleMetric.commentCount,
            newsArticleMetric.viewCount,
            JPAExpressions.selectOne()
                .from(articleView)
                .where(articleView.id.articleId.eq(newsArticle.id)
                    .and(articleView.id.viewerId.eq(userId)))
                .exists()
        ))
        .from(newsArticle)
        .join(newsArticleMetric).on(newsArticle.id.eq(newsArticleMetric.id))
        .where(
            newsArticle.deletedAt.isNull(),
            hasKeyword(request.keyword()),
            interestIdEquals(request.interestId()),
            publishDateBetween(request.publishDateFrom(), request.publishDateTo()),
            sourceIn(request.sourceIn()),
            cursorCondition(request)
        )
        .orderBy(getOrderSpecifiers(request))
        .limit(request.limit() + 1)
        .fetch();
  }

  private BooleanExpression hasKeyword(String keyword) {
    return keyword != null && !keyword.trim().isEmpty()
        ? newsArticle.title.containsIgnoreCase(keyword)
        .or(newsArticle.summary.containsIgnoreCase(keyword))
        : null;
  }

  private BooleanExpression interestIdEquals(Long interestId) {
    if (interestId == null) {
      return null;
    }

    return newsArticle.id.in(
        JPAExpressions.select(articleInterest.id.articleId)
            .from(articleInterest)
            .where(articleInterest.id.interestId.eq(interestId))
    );
  }

  private BooleanExpression sourceIn(List<String> sources) {
    return sources != null && !sources.isEmpty()
        ? newsArticle.sourceName.in(sources)
        : null;
  }

  private BooleanExpression publishDateBetween(LocalDateTime from, LocalDateTime to) {
    BooleanExpression condition = null;
    ZoneId KST = ZoneId.of("Asia/Seoul");

    if (from != null) {
      Instant kstStart = from
              .toLocalDate()
              .atStartOfDay(KST)
              .toInstant();
      condition = newsArticle.publishedAt.goe(kstStart);
    }

    if (to != null) {
      Instant kstEnd = to
              .toLocalDate()
              .plusDays(1)
              .atStartOfDay(KST)
              .toInstant();
      BooleanExpression toCondition = newsArticle.publishedAt.lt(kstEnd);

      condition = (condition == null) ? toCondition : condition.and(toCondition);
    }

    return condition;
  }

  private BooleanExpression cursorCondition(CursorPageArticleRequest request) {
    if (request.cursor() == null || request.after() == null) {
      return null;
    }

    Instant after = request.after();
    String direction = request.getDirectionWithDefault();

    switch (request.getOrderByWithDefault()) {
      case "publishdate":
        if ("desc".equals(direction)) {
          return newsArticle.publishedAt.lt(after);
        } else {
          return newsArticle.publishedAt.gt(after);
        }

      case "commentcount":
        long commentCount = Long.parseLong(request.cursor());
        if ("desc".equals(direction)) {
          return newsArticleMetric.commentCount.lt(commentCount)
              .or(newsArticleMetric.commentCount.eq(commentCount)
                  .and(newsArticle.publishedAt.lt(after)));
        } else {
          return newsArticleMetric.commentCount.gt(commentCount)
              .or(newsArticleMetric.commentCount.eq(commentCount)
                  .and(newsArticle.publishedAt.lt(after)));
        }

      case "viewcount":
        long viewCount = Long.parseLong(request.cursor());
        if ("desc".equals(direction)) {
          return newsArticleMetric.viewCount.lt(viewCount)
              .or(newsArticleMetric.viewCount.eq(viewCount)
                  .and(newsArticle.publishedAt.lt(after)));
        } else {
          return newsArticleMetric.viewCount.gt(viewCount)
              .or(newsArticleMetric.viewCount.eq(viewCount)
                  .and(newsArticle.publishedAt.lt(after)));
        }

      default:
        return null;
    }
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(CursorPageArticleRequest request) {
    List<OrderSpecifier<?>> specifiers = new ArrayList<>();
    Order direction = ("asc".equalsIgnoreCase(request.direction())) ? Order.ASC : Order.DESC;

    switch (request.getOrderByWithDefault()) {
      case "commentcount":
        specifiers.add(new OrderSpecifier<>(direction, newsArticleMetric.commentCount));
        specifiers.add(new OrderSpecifier<>(Order.DESC, newsArticle.publishedAt));
        break;
      case "viewcount":
        specifiers.add(new OrderSpecifier<>(direction, newsArticleMetric.viewCount));
        specifiers.add(new OrderSpecifier<>(Order.DESC, newsArticle.publishedAt));
        break;
      default:
        specifiers.add(new OrderSpecifier<>(direction, newsArticle.publishedAt));
    }

    specifiers.add(new OrderSpecifier<>(direction, newsArticle.id));

    return specifiers.toArray(new OrderSpecifier[0]);
  }
}
