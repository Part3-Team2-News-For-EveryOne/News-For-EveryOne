package com.example.newsforeveryone.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.newsforeveryone.comment.entity.Comment;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.dto.CommentUpdateRequest;
import com.example.newsforeveryone.comment.repository.CommentLikeRepository;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;

class CommentServiceTest extends IntegrationTestSupport {

  @Autowired
  private CommentService commentService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommentLikeRepository commentLikeRepository;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private NewsArticleRepository newsArticleRepository;

  private User testUser1;
  private User testUser2;
  private Long articleId;

  @BeforeEach
  void setUp() {
    // 테스트용 사용자 생성
    testUser1 = User.builder()
        .email("test1@example.com")
        .nickname("testUser1")
        .password("Password123!")
        .build();

    testUser2 = User.builder()
        .email("test321@example.com")
        .nickname("testUser2")
        .password("Password123!")
        .build();

    testUser1 = userRepository.save(testUser1);
    testUser2 = userRepository.save(testUser2);

    NewsArticle savedNewsArticle = saveNewsArticle("");
    articleId = savedNewsArticle.getId();
  }

  @AfterEach
  void tearDown() {
    commentLikeRepository.deleteAllInBatch();
    commentRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
    newsArticleRepository.deleteAllInBatch();
  }

  @DisplayName("댓글에 좋아요 추가 테스트")
  @Test
  void testAddCommentLike() {
    // given
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // when
    CommentLikeResponse likeResponse = commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId());

    // then
    assertThat(likeResponse.commentLikeCount()).isEqualTo(1L);
    assertThat(commentLikeRepository.existsByCommentIdAndLikedUserId(Long.valueOf(comment.id()), testUser2.getId()))
        .isTrue();
  }

  @DisplayName("댓글에 추가된 좋아요 취소 테스트")
  @Test
  void testCancelCommentLike() {
    // given
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());
    commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId());

    // when
    commentService.unlikeComment(Long.valueOf(comment.id()), testUser2.getId());

    // then
    assertThat(commentLikeRepository.existsByCommentIdAndLikedUserId(Long.valueOf(comment.id()), testUser2.getId()))
        .isFalse();
  }

  @DisplayName("댓글 좋아요 중복 방지 테스트")
  @Test
  void testPreventDuplicateCommentLike() {
    // given: 댓글 생성 및 첫 번째 좋아요
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());
    commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId());

    // when & then: 동일 사용자가 다시 좋아요 시도 시 예외 발생
    assertThatThrownBy(
        () -> commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_LIKE_DUPLICATED);
  }

  @DisplayName("존재하지 않는 좋아요 취소 시 예외 발생 테스트")
  @Test
  void testUnlikeNonExistentCommentLike() {
    // given: 댓글 생성 (좋아요는 하지 않음)
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // when & then: 좋아요하지 않은 댓글의 좋아요 취소 시 예외 발생
    assertThatThrownBy(
        () -> commentService.unlikeComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_LIKE_NOT_FOUND);
  }

  @DisplayName("본인 댓글만 수정 가능 테스트")
  @Test
  void testUpdateCommentPermission() {
    // given: user1이 댓글 작성
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "원본 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // when: user1이 본인 댓글 수정 (성공)
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정된 댓글");
    CommentResponse updatedComment = commentService.updateComment(
        Long.valueOf(comment.id()), updateRequest, testUser1.getId());

    // then: 댓글이 정상적으로 수정됨
    assertThat(updatedComment.content()).isEqualTo("수정된 댓글");

    // when & then: user2가 user1의 댓글 수정 시도 시 예외 발생
    assertThatThrownBy(() -> commentService.updateComment(
        Long.valueOf(comment.id()), updateRequest, testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_UPDATE_FORBIDDEN);
  }

  @DisplayName("본인 댓글만 삭제 가능 테스트")
  @Test
  void testDeleteCommentPermission() {
    // given: user1이 댓글 작성
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "삭제할 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // when & then: user2가 user1의 댓글 삭제 시도 시 예외 발생
    assertThatThrownBy(
        () -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_DELETE_FORBIDDEN);

    // when: user1이 본인 댓글 삭제 (성공)
    assertThatCode(
        () -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser1.getId()))
        .doesNotThrowAnyException();
  }

  @DisplayName("소프트 삭제된 댓글 접근 차단 테스트")
  @Test
  void testSoftDeletedCommentAccess() {
    // given: 댓글 생성 및 소프트 삭제
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "삭제될 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());
    commentService.softDeleteComment(Long.valueOf(comment.id()), testUser1.getId());

    // when & then: 삭제된 댓글 수정 시도 시 예외 발생
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정 시도");
    assertThatThrownBy(() -> commentService.updateComment(
        Long.valueOf(comment.id()), updateRequest, testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ALREADY_DELETED);

    // when & then: 삭제된 댓글 재삭제 시도 시 예외 발생
    assertThatThrownBy(
        () -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ALREADY_DELETED);

    // when & then: 삭제된 댓글 좋아요 시도 시 예외 발생 (댓글을 찾을 수 없음)
    assertThatThrownBy(
        () -> commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @DisplayName("좋아요 수 기준 정렬 페이지네이션 테스트")
  @Test
  void testLikeCountBasedPagination() {
    // given: 댓글 생성 및 서로 다른 좋아요 수 설정
    CommentCreateRequest request1 = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "댓글1");
    CommentResponse comment1 = commentService.createComment(request1, testUser1.getId());

    CommentCreateRequest request2 = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "댓글2");
    CommentResponse comment2 = commentService.createComment(request2, testUser1.getId());

    CommentCreateRequest request3 = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "댓글3");
    CommentResponse comment3 = commentService.createComment(request3, testUser1.getId());

    // comment2에 좋아요 1개, comment3에 좋아요 2개 추가
    commentService.likeComment(Long.valueOf(comment2.id()), testUser2.getId());

    User testUser3 = User.builder().email("test3@example.com").nickname("testUser3")
        .password("Password123!").build();
    testUser3 = userRepository.save(testUser3);

    commentService.likeComment(Long.valueOf(comment3.id()), testUser2.getId());
    commentService.likeComment(Long.valueOf(comment3.id()), testUser3.getId());

    // when: 좋아요 수 내림차순으로 조회
    CommentListResponse response = commentService.getComments(
        articleId, "likeCount", "DESC", null, null, 10, testUser1.getId());

    // then: 좋아요 수 순서대로 정렬되어 있는지 확인
    List<CommentResponse> comments = response.content();
    assertThat(comments).hasSize(3);

    // 좋아요 수가 많은 순서대로 정렬되어야 함: comment3(2) > comment2(1) > comment1(0)
    assertThat(comments.get(0).likeCount()).isEqualTo(2L);
    assertThat(comments.get(1).likeCount()).isEqualTo(1L);
    assertThat(comments.get(2).likeCount()).isEqualTo(0L);
  }

  @DisplayName("사용자별 좋아요 상태 확인 테스트")
  @Test
  void testUserLikedStatusInCommentList() {
    // given: 댓글 생성
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // user2가 좋아요 추가
    commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId());

    // when: user2가 댓글 목록 조회
    CommentListResponse responseForUser2 = commentService.getComments(
        articleId, "createdAt", "DESC", null, null, 10, testUser2.getId());

    // then: user2에게는 좋아요 상태가 true로 표시
    CommentResponse commentForUser2 = responseForUser2.content().get(0);
    assertThat(commentForUser2.likedByMe()).isTrue();

    // when: user1이 댓글 목록 조회
    CommentListResponse responseForUser1 = commentService.getComments(
        articleId, "createdAt", "DESC", null, null, 10, testUser1.getId());

    // then: user1에게는 좋아요 상태가 false로 표시
    CommentResponse commentForUser1 = responseForUser1.content().get(0);
    assertThat(commentForUser1.likedByMe()).isFalse();
  }

  @DisplayName("존재하지 않는 댓글 접근 시 예외 발생 테스트")
  @Test
  void testAccessNonExistentComment() {
    Long nonExistentCommentId = 999999L;

    // when & then: 존재하지 않는 댓글 좋아요 시도
    assertThatThrownBy(() -> commentService.likeComment(nonExistentCommentId, testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);

    // when & then: 존재하지 않는 댓글 수정 시도
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정 내용");
    assertThatThrownBy(() -> commentService.updateComment(
        nonExistentCommentId, updateRequest, testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);

    // when & then: 존재하지 않는 댓글 삭제 시도
    assertThatThrownBy(
        () -> commentService.softDeleteComment(nonExistentCommentId, testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Nested
  @DisplayName("커서 기반 페이지네이션 정렬 테스트")
  class CursorPaginationTests {

    private List<Comment> comments = new ArrayList<>();

    @BeforeEach
    void setupComments() throws InterruptedException {
      // 테스트를 위한 댓글 5개 생성 (생성 시간 간격을 두어 순서 보장)
      for (int i = 1; i <= 5; i++) {
        Comment comment = Comment.builder()
            .articleId(articleId)
            .userId(testUser1.getId())
            .content("댓글 " + i)
            .build();
        comments.add(commentRepository.save(comment));
        Thread.sleep(10); // createdAt 순서를 명확하게 하기 위함
      }
      Collections.reverse(comments); // 최신순으로 정렬 (댓글 5, 4, 3, 2, 1)
    }

    @DisplayName("최신순으로 페이지네이션 동작")
    @Test
    void testPaginationByCreatedAtDesc() {
      // given : 페이지 크기는 2
      int limit = 2;

      // when : 첫 페이지 조회
      CommentListResponse page1 = commentService.getComments(articleId, "createdAt", "DESC", null,
          null, limit, testUser1.getId());

      // then : 첫 페이지 결과 검증
      assertThat(page1.content()).hasSize(limit);
      assertThat(page1.hasNext()).isTrue();
      // 최신 댓글 2개 (댓글 5, 댓글 4)가 순서대로 오는지 확인
      assertThat(page1.content().get(0).id()).isEqualTo(String.valueOf(comments.get(0).getId()));
      assertThat(page1.content().get(1).id()).isEqualTo(String.valueOf(comments.get(1).getId()));

      // given : 첫 페이지의 마지막 댓글을 커서로 사용
      String cursor = page1.nextCursor();
      Long after = Long.valueOf(page1.nextAfter());

      // when : 두 번째 페이지 조회
      CommentListResponse page2 = commentService.getComments(articleId, "createdAt", "DESC",
          cursor, after, limit, testUser1.getId());

      // then : 두 번째 페이지 결과 검증
      assertThat(page2.content()).hasSize(limit);
      assertThat(page2.hasNext()).isTrue();
      // 다음 최신 댓글 2개 (댓글 3, 댓글 2)가 순서대로 오는지 확인
      assertThat(page2.content().get(0).id()).isEqualTo(String.valueOf(comments.get(2).getId()));
      assertThat(page2.content().get(1).id()).isEqualTo(String.valueOf(comments.get(3).getId()));

      // given : 두 번째 페이지의 마지막 댓글을 커서로 사용
      cursor = page2.nextCursor();
      after = Long.valueOf(page2.nextAfter());

      // when : 세 번째 페이지 조회
      CommentListResponse page3 = commentService.getComments(articleId, "createdAt", "DESC",
          cursor, after, limit, testUser1.getId());

      // then : 세 번째 페이지 결과 검증
      assertThat(page3.content()).hasSize(1);
      assertThat(page3.hasNext()).isFalse();
      // 마지막 댓글 (댓글 1)이 오는지 확인
      assertThat(page3.content().get(0).id()).isEqualTo(String.valueOf(comments.get(4).getId()));
    }

    @DisplayName("오래된순으로 페이지네이션 동작")
    @Test
    void testPaginationByCreatedAtAsc() {
      // given
      int limit = 2;
      List<Comment> oldestFirst = comments.stream()
          .sorted(Comparator.comparing(Comment::getCreatedAt))
          .collect(Collectors.toList());

      // when : 첫 페이지 조회
      CommentListResponse page1 = commentService.getComments(articleId, "createdAt", "ASC", null,
          null, limit, testUser1.getId());

      // then : 첫 페이지 결과 검증
      assertThat(page1.content()).hasSize(limit);
      assertThat(page1.hasNext()).isTrue();
      assertThat(page1.content().get(0).id()).isEqualTo(String.valueOf(oldestFirst.get(0).getId()));
      assertThat(page1.content().get(1).id()).isEqualTo(String.valueOf(oldestFirst.get(1).getId()));

      // given
      String cursor = page1.nextCursor();
      Long after = Long.valueOf(page1.nextAfter());

      // when
      CommentListResponse page2 = commentService.getComments(articleId, "createdAt", "ASC", cursor,
          after, limit, testUser1.getId());

      // then
      assertThat(page2.content()).hasSize(limit);
      assertThat(page2.hasNext()).isTrue();
      assertThat(page2.content().get(0).id()).isEqualTo(String.valueOf(oldestFirst.get(2).getId()));
      assertThat(page2.content().get(1).id()).isEqualTo(String.valueOf(oldestFirst.get(3).getId()));
    }

    @DisplayName("좋아요 많은순으로 페이지네이션 동작")
    @Test
    void testPaginationByLikeCountDesc() {
      // given: 댓글마다 좋아요 수를 다르게 설정
      // 댓글 5: 좋아요 3개
      // 댓글 4: 좋아요 2개
      // 댓글 3: 좋아요 2개 (ID는 댓글 4보다 작음)
      // 댓글 2: 좋아요 1개
      // 댓글 1: 좋아요 0개
      addLikes(comments.get(0), 3); // 댓글 5
      addLikes(comments.get(1), 2); // 댓글 4
      addLikes(comments.get(2), 2); // 댓글 3
      addLikes(comments.get(3), 1); // 댓글 2

      // 예상 정렬 순서: 댓글5(3) > 댓글4(2) > 댓글3(2) > 댓글2(1) > 댓글1(0)
      // ID는 DESC로 2차 정렬되므로, 좋아요 수가 같으면 ID가 큰 댓글4가 댓글3보다 먼저 와야 함
      List<Comment> expectedOrder = List.of(
          comments.get(0), comments.get(1), comments.get(2), comments.get(3), comments.get(4)
      );
      int limit = 2;

      // when : 첫 페이지 조회
      CommentListResponse page1 = commentService.getComments(articleId, "likeCount", "DESC", null,
          null, limit, testUser1.getId());

      // then : 첫 페이지 결과 검증
      assertThat(page1.content()).hasSize(limit);
      assertThat(page1.hasNext()).isTrue();
      assertThat(page1.content().get(0).id()).isEqualTo(String.valueOf(expectedOrder.get(0).getId()));
      assertThat(page1.content().get(1).id()).isEqualTo(String.valueOf(expectedOrder.get(1).getId()));
      assertThat(page1.content().get(0).likeCount()).isEqualTo(3L);
      assertThat(page1.content().get(1).likeCount()).isEqualTo(2L);


      // given
      String cursor = page1.nextCursor(); // 이 값은 사용되지 않음 (구현상 after만 사용)
      Long after = Long.valueOf(page1.nextAfter());

      // when : 두 번째 페이지 조회
      CommentListResponse page2 = commentService.getComments(articleId, "likeCount", "DESC",
          cursor, after, limit, testUser1.getId());

      // then : 두 번째 페이지 결과 검증
      assertThat(page2.content()).hasSize(limit);
      assertThat(page2.hasNext()).isTrue();
      assertThat(page2.content().get(0).id()).isEqualTo(String.valueOf(expectedOrder.get(2).getId()));
      assertThat(page2.content().get(1).id()).isEqualTo(String.valueOf(expectedOrder.get(3).getId()));
      assertThat(page2.content().get(0).likeCount()).isEqualTo(2L);
      assertThat(page2.content().get(1).likeCount()).isEqualTo(1L);

      // given
      cursor = page2.nextCursor();
      after = Long.valueOf(page2.nextAfter());

      // when : 세 번째 페이지 조회
      CommentListResponse page3 = commentService.getComments(articleId, "likeCount", "DESC",
          cursor, after, limit, testUser1.getId());

      // then : 세 번째 페이지 결과 검증
      assertThat(page3.content()).hasSize(1);
      assertThat(page3.hasNext()).isFalse();
      assertThat(page3.content().get(0).id()).isEqualTo(String.valueOf(expectedOrder.get(4).getId()));
      assertThat(page3.content().get(0).likeCount()).isEqualTo(0L);
    }

    private void addLikes(Comment comment, int count) {
      // 좋아요 추가를 위해 임시 사용자 생성 및 저장
      for (int i = 0; i < count; i++) {
        User tempUser = userRepository.save(User.builder()
            .email("likeUser" + comment.getId() + "_" + i + "@test.com")
            .nickname("likeUser" + i)
            .password("password")
            .build());
        commentService.likeComment(comment.getId(), tempUser.getId());
      }
    }
  }

  private NewsArticle saveNewsArticle(String link) {
    NewsArticle newsArticle = NewsArticle.builder()
        .interestIds(null)
        .title("")
        .summary("")
        .link(link)
        .sourceName("")
        .publishedAt(Instant.now())
        .build();

    return newsArticleRepository.save(newsArticle);
  }

}

