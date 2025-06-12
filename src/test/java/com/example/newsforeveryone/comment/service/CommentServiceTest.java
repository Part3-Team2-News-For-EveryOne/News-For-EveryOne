package com.example.newsforeveryone.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.newsforeveryone.IntegrationTestSupport;
import com.example.newsforeveryone.comment.dto.CommentCreateRequest;
import com.example.newsforeveryone.comment.dto.CommentLikeResponse;
import com.example.newsforeveryone.comment.dto.CommentListResponse;
import com.example.newsforeveryone.comment.dto.CommentResponse;
import com.example.newsforeveryone.comment.dto.CommentUpdateRequest;
import com.example.newsforeveryone.comment.repository.CommentLikeRepository;
import com.example.newsforeveryone.comment.repository.CommentRepository;
import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
class CommentServiceTest extends IntegrationTestSupport {

  @Autowired
  private CommentService commentService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommentLikeRepository commentLikeRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser1;
  private User testUser2;
  private Long articleId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트용 사용자 생성
    testUser1 = User.builder()
        .email("test1@example.com")
        .nickname("testUser1")
        .build();

    testUser2 = User.builder()
        .email("test2@example.com")
        .nickname("testUser2")
        .build();

    testUser1 = userRepository.save(testUser1);
    testUser2 = userRepository.save(testUser2);
  }

  @DisplayName("댓글 좋아요 기능 테스트")
  @Test
  void testCommentLikeFeature() {
    // given: 댓글 생성
    CommentCreateRequest createRequest = new CommentCreateRequest(
        String.valueOf(articleId), String.valueOf(testUser1.getId()), "테스트 댓글");
    CommentResponse comment = commentService.createComment(createRequest, testUser1.getId());

    // when: 댓글에 좋아요 추가
    CommentLikeResponse likeResponse = commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId());

    // then: 좋아요가 정상적으로 추가됨
    assertThat(likeResponse.commentLikeCount()).isEqualTo(1L);
    assertThat(
        commentLikeRepository.existsByCommentIdAndLikedUserId(Long.valueOf(comment.id()), testUser2.getId()))
        .isTrue();

    // when: 좋아요 취소
    commentService.unlikeComment(Long.valueOf(comment.id()), testUser2.getId());

    // then: 좋아요가 정상적으로 취소됨
    assertThat(
        commentLikeRepository.existsByCommentIdAndLikedUserId(Long.valueOf(comment.id()), testUser2.getId()))
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
    assertThatThrownBy(() -> commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId()))
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
    assertThatThrownBy(() -> commentService.unlikeComment(Long.valueOf(comment.id()), testUser2.getId()))
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
    assertThatThrownBy(() -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_DELETE_FORBIDDEN);

    // when: user1이 본인 댓글 삭제 (성공)
    assertThatCode(() -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser1.getId()))
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
    assertThatThrownBy(() -> commentService.softDeleteComment(Long.valueOf(comment.id()), testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ALREADY_DELETED);

    // when & then: 삭제된 댓글 좋아요 시도 시 예외 발생 (댓글을 찾을 수 없음)
    assertThatThrownBy(() -> commentService.likeComment(Long.valueOf(comment.id()), testUser2.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @DisplayName("커서 기반 페이지네이션 동작 테스트")
  @Test
  void testCursorBasedPagination() {
    // given: 여러 댓글 생성 (시간 간격을 두고)
    for (int i = 1; i <= 5; i++) {
      CommentCreateRequest request = new CommentCreateRequest(
          String.valueOf(articleId), String.valueOf(testUser1.getId()), "댓글 " + i);
      commentService.createComment(request, testUser1.getId());

      // 시간 간격을 위한 약간의 지연
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    // when: 첫 번째 페이지 조회 (limit=2)
    CommentListResponse firstPage = commentService.getComments(
        articleId, "createdAt", "DESC", null, null, 2, testUser1.getId());

    // then: 첫 번째 페이지 검증
    assertThat(firstPage.content()).hasSize(2);
    assertThat(firstPage.hasNext()).isTrue();
    assertThat(firstPage.nextCursor()).isNotNull();
    assertThat(firstPage.nextAfter()).isNotNull(); // nextAfter 필드 검증 추가

    // when: 두 번째 페이지 조회 (커서 사용)
    CommentListResponse secondPage = commentService.getComments(
        articleId, "createdAt", "DESC", firstPage.nextCursor(), Long.valueOf(firstPage.nextAfter()), 2, testUser1.getId());

    // then: 두 번째 페이지 검증
    assertThat(secondPage.content()).hasSize(2);
    assertThat(secondPage.hasNext()).isTrue();
    assertThat(secondPage.nextAfter()).isNotNull(); // nextAfter 필드 검증 추가

    // 첫 번째 페이지와 두 번째 페이지의 댓글이 다른지 확인
    List<String> firstPageIds = firstPage.content().stream()
        .map(CommentResponse::id).toList();
    List<String> secondPageIds = secondPage.content().stream()
        .map(CommentResponse::id).toList();

    assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);

    // when: 마지막 페이지 조회
    CommentListResponse lastPage = commentService.getComments(
        articleId, "createdAt", "DESC", secondPage.nextCursor(), Long.valueOf(secondPage.nextAfter()), 2, testUser1.getId());

    // then: 마지막 페이지 검증
    assertThat(lastPage.content()).hasSize(1); // 남은 댓글 1개
    assertThat(lastPage.hasNext()).isFalse();
    assertThat(lastPage.nextCursor()).isNull();
    assertThat(lastPage.nextAfter()).isNull(); // nextAfter 필드 검증 추가
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

    User testUser3 = User.builder().email("test3@example.com").nickname("testUser3").build();
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
    assertThatThrownBy(() -> commentService.softDeleteComment(nonExistentCommentId, testUser1.getId()))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

}
