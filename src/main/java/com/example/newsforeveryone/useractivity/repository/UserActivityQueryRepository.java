package com.example.newsforeveryone.useractivity.repository;

import com.example.newsforeveryone.user.entity.QUser;
import com.example.newsforeveryone.useractivity.repository.projection.ArticleViewActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentLikeActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.QArticleViewActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.QCommentActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.QCommentLikeActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.QSubscriptionActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.SubscriptionActivityProjection;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.newsforeveryone.comment.entity.QComment.comment;
import static com.example.newsforeveryone.comment.entity.QCommentLike.commentLike;
import static com.example.newsforeveryone.interest.entity.QInterest.interest;
import static com.example.newsforeveryone.interest.entity.QInterestKeyword.interestKeyword;
import static com.example.newsforeveryone.interest.entity.QKeyword.keyword;
import static com.example.newsforeveryone.interest.entity.QSubscription.subscription;
import static com.example.newsforeveryone.newsarticle.entity.QArticleView.articleView;
import static com.example.newsforeveryone.newsarticle.entity.QNewsArticle.newsArticle;
import static com.example.newsforeveryone.newsarticle.entity.QNewsArticleMetric.newsArticleMetric;
import static com.example.newsforeveryone.user.entity.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;


@Repository
@RequiredArgsConstructor
public class UserActivityQueryRepository implements UserActivityRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<SubscriptionActivityProjection> findSubscriptionActivities(Long userId) {
    List<Tuple> results = queryFactory
        .select(
            interest.id,
            interest.name,
            keyword.name,
            interest.subscriberCount.longValue(),
            subscription.createdAt
        )
        .from(subscription)
        .join(subscription.interest, interest)
        .leftJoin(interestKeyword).on(interestKeyword.interest.id.eq(interest.id))
        .leftJoin(interestKeyword.keyword, keyword)
        .where(subscription.id.userId.eq(userId))
        .orderBy(subscription.createdAt.desc())
        .fetch();

    Map<Long, List<Tuple>> grouped = results.stream()
        .collect(Collectors.groupingBy(t -> t.get(interest.id)));

    return grouped.entrySet().stream()
        .map(e -> {
          List<Tuple> tuples = e.getValue();
          return new SubscriptionActivityProjection(
              null,
              e.getKey(),
              tuples.get(0).get(interest.name),
              tuples.stream()
                  .map(t -> t.get(keyword.name))
                  .filter(Objects::nonNull)
                  .distinct()
                  .toList(),
              tuples.get(0).get(interest.subscriberCount.longValue()),
              tuples.get(0).get(subscription.createdAt)
          );
        })
        .toList();
  }

  @Override
  public List<CommentActivityProjection> findCommentActivities(Long userId, int limit) {
    return queryFactory
        .select(new QCommentActivityProjection(
            comment.id,
            comment.articleId,
            newsArticle.title,
            comment.userId,
            user.nickname,
            comment.content,
            comment.likes.size().longValue(),
            comment.createdAt
        ))
        .from(comment)
        .join(newsArticle).on(comment.articleId.eq(newsArticle.id))
        .join(user).on(comment.userId.eq(user.id))
        .where(comment.userId.eq(userId).and(comment.deletedAt.isNull()))
        .orderBy(comment.createdAt.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<CommentLikeActivityProjection> findCommentLikeActivities(Long userId, int limit) {
    QUser commenter = new QUser("commenter");

    return queryFactory
        .select(new QCommentLikeActivityProjection(
            nullExpression(Long.class),
            commentLike.likedAt,
            commentLike.commentId,
            comment.articleId,
            newsArticle.title,
            comment.userId,
            commenter.nickname,
            comment.content,
            comment.likes.size().longValue(),
            comment.createdAt
        ))
        .from(commentLike)
        .join(comment).on(commentLike.commentId.eq(comment.id))
        .join(newsArticle).on(comment.articleId.eq(newsArticle.id))
        .join(commenter).on(comment.userId.eq(commenter.id))
        .where(commentLike.likedUserId.eq(userId).and(comment.deletedAt.isNull()))
        .orderBy(commentLike.likedAt.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<ArticleViewActivityProjection> findArticleViewActivities(Long userId, int limit) {
    return queryFactory
        .select(new QArticleViewActivityProjection(
            nullExpression(Long.class),
            articleView.id.viewerId,
            articleView.viewedAt,
            articleView.id.articleId,
            newsArticle.sourceName,
            newsArticle.link,
            newsArticle.title,
            newsArticle.publishedAt,
            newsArticle.summary,
            newsArticleMetric.commentCount,
            newsArticleMetric.viewCount
        ))
        .from(articleView)
        .join(newsArticle).on(articleView.id.articleId.eq(newsArticle.id))
        .join(newsArticleMetric).on(newsArticle.id.eq(newsArticleMetric.id))
        .where(articleView.id.viewerId.eq(userId).and(newsArticle.deletedAt.isNull()))
        .orderBy(articleView.viewedAt.desc())
        .limit(limit)
        .fetch();
  }
}
