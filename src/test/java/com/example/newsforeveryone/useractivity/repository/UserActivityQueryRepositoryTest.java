package com.example.newsforeveryone.useractivity.repository;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.comment.entity.CommentLike;
import com.example.newsforeveryone.comment.repository.CommentLikeRepository;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.Subscription;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.newsarticle.entity.ArticleView;
import com.example.newsforeveryone.newsarticle.entity.ArticleViewId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.ArticleViewRepository;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import com.example.newsforeveryone.useractivity.repository.projection.ArticleViewActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.CommentLikeActivityProjection;
import com.example.newsforeveryone.useractivity.repository.projection.SubscriptionActivityProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@Transactional
class UserActivityQueryRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private UserActivityQueryRepository userActivityQueryRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private NewsArticleRepository newsArticleRepository;
  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private KeywordRepository keywordRepository;
  @Autowired
  private InterestKeywordRepository interestKeywordRepository;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private CommentLikeRepository commentLikeRepository;
  @Autowired
  private ArticleViewRepository articleViewRepository;


  @Nested
  @DisplayName("최근 작성한 댓글 테스트")
  class FindCommentActivitiesTest {

    @DisplayName("사용자가 자신이 작성한 댓글 활동 내역을 최신순으로 조회할 수 있다.")
    @Test
    void success() {
      // given
      User user = createUser("user@test.com", "댓글쓴사람");
      NewsArticle article = createArticle("테스트 뉴스");
      createComment(user, article, "오래된 댓글");
      createComment(user, article, "최신 댓글");

      User anotherUser = createUser("another@test.com", "다른사용자");
      createComment(anotherUser, article, "다른 사용자 댓글");

      // when
      List<CommentActivityProjection> projections = userActivityQueryRepository.findCommentActivities(user.getId(), 10);

      // then
      assertThat(projections).hasSize(2)
          .extracting(CommentActivityProjection::content)
          .containsExactly("최신 댓글", "오래된 댓글");
    }

    @DisplayName("사용자의 활동 내역이 없을 경우, 빈 리스트를 반환한다.")
    @Test
    void whenNoActivity_thenReturnEmptyList() {
      // given
      User user = createUser("user@test.com", "본인");

      // when
      List<CommentActivityProjection> projections = userActivityQueryRepository.findCommentActivities(user.getId(), 10);

      // then
      assertThat(projections).isEmpty();
    }
  }

  @Nested
  @DisplayName("구독 내역 조회 테스트")
  class FindSubscriptionActivitiesTest {

    @DisplayName("특정 사용자의 구독 활동 내역과 관련 키워드 목록을 조회할 수 있다.")
    @Test
    void success() {
      // given
      User user = createUser("user@test.com", "구독한사람");

      Interest interestWithKeywords = createInterest("축구");
      Keyword keyword1 = createKeyword("메시");
      Keyword keyword2 = createKeyword("호날두");
      createInterestKeyword(interestWithKeywords, keyword1);
      createInterestKeyword(interestWithKeywords, keyword2);

      Interest interestWithoutKeywords = createInterest("정치");
      createSubscription(user, interestWithKeywords);
      createSubscription(user, interestWithoutKeywords);

      // when
      List<SubscriptionActivityProjection> projections = userActivityQueryRepository.findSubscriptionActivities(user.getId());

      // then
      assertThat(projections).hasSize(2)
          .extracting(SubscriptionActivityProjection::interestName, SubscriptionActivityProjection::interestKeywords)
          .containsExactlyInAnyOrder(
              tuple("축구", List.of("메시", "호날두")),
              tuple("정치", List.of())
          );
    }
  }

  @Nested
  @DisplayName("댓글 좋아요 활동 내역 조회 테스트")
  class FindCommentLikeActivitiesTest {

    @DisplayName("특정 사용자가 '좋아요'를 누른 댓글 활동 내역을 최신순으로 조회할 수 있다.")
    @Test
    void success() {
      // given
      User likingUser = createUser("liker@test.com", "좋아요누른사람");
      User commentAuthor = createUser("author@test.com", "댓글작성자");
      NewsArticle article = createArticle("테스트 기사");
      Comment comment1 = createComment(commentAuthor, article, "옛날 댓글");
      Comment comment2 = createComment(commentAuthor, article, "최신 댓글");
      createCommentLike(likingUser, comment1);
      createCommentLike(likingUser, comment2);

      // when
      List<CommentLikeActivityProjection> projections = userActivityQueryRepository.findCommentLikeActivities(likingUser.getId(), 10);

      // then
      assertThat(projections).hasSize(2)
          .extracting(CommentLikeActivityProjection::commentContent)
          .containsExactly("최신 댓글", "옛날 댓글");
    }
  }

  @Nested
  @DisplayName("기사 본 내역 조회 테스트")
  class FindArticleViewActivitiesTest {

    @DisplayName("특정 사용자가 본 기사 내역을 최신순으로 조회할 수 있다.")
    @Test
    void success() {
      // given
      User viewer = createUser("viewer@test.com", "조회한사람");
      NewsArticle article1 = createArticle("먼저 본 기사");
      NewsArticle article2 = createArticle("뒤에 본 기사");
      createArticleView(viewer, article1);
      createArticleView(viewer, article2);

      // when
      List<ArticleViewActivityProjection> projections = userActivityQueryRepository.findArticleViewActivities(viewer.getId(), 10);

      // then
      assertThat(projections).hasSize(2)
          .extracting(ArticleViewActivityProjection::articleTitle)
          .containsExactly("뒤에 본 기사", "먼저 본 기사");
    }
  }


  private User createUser(String email, String nickname) {
    User user = User.builder()
        .email(email)
        .nickname(nickname)
        .password("password")
        .build();
    return userRepository.save(user);
  }

  private NewsArticle createArticle(String title) {
    NewsArticle article = NewsArticle.builder()
        .sourceName("테스트 언론사")
        .link("https://test.com/" + title)
        .title(title)
        .summary("요약")
        .publishedAt(Instant.now())
        .build();
    return newsArticleRepository.save(article);
  }

  private Comment createComment(User user, NewsArticle article, String content) {
    Comment comment = Comment.builder()
        .userId(user.getId())
        .articleId(article.getId())
        .content(content)
        .build();
    return commentRepository.save(comment);
  }

  private Interest createInterest(String name) {
    return interestRepository.save(new Interest(name));
  }

  private Keyword createKeyword(String name) {
    return keywordRepository.save(new Keyword(name));
  }

  private InterestKeyword createInterestKeyword(Interest interest, Keyword keyword) {
    return interestKeywordRepository.save(new InterestKeyword(interest, keyword));
  }

  private Subscription createSubscription(User user, Interest interest) {
    return subscriptionRepository.save(new Subscription(interest, user.getId()));
  }

  private CommentLike createCommentLike(User user, Comment comment) {
    return commentLikeRepository.save(CommentLike.builder()
        .likedUserId(user.getId())
        .commentId(comment.getId())
        .build());
  }

  private ArticleView createArticleView(User user, NewsArticle article) {
    return articleViewRepository.save(new ArticleView(new ArticleViewId(article.getId(), user.getId()), Instant.now()));
  }

}