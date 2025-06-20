package com.example.newsforeveryone.interest.service;

import com.example.newsforeveryone.interest.dto.InterestResult;
import com.example.newsforeveryone.interest.dto.SubscriptionResult;
import com.example.newsforeveryone.interest.dto.request.InterestRegisterRequest;
import com.example.newsforeveryone.interest.dto.request.InterestSearchRequest;
import com.example.newsforeveryone.interest.dto.request.InterestUpdateRequest;
import com.example.newsforeveryone.interest.dto.response.CursorPageInterestResponse;
import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.entity.id.SubscriptionId;
import com.example.newsforeveryone.interest.exception.InterestNotFoundException;
import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import com.example.newsforeveryone.interest.repository.InterestRepository;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import com.example.newsforeveryone.interest.repository.SubscriptionRepository;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterest;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.ArticleInterestRepository;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.support.IntegrationTestSupport;
import com.example.newsforeveryone.user.entity.User;
import com.example.newsforeveryone.user.exception.UserNotFoundException;
import com.example.newsforeveryone.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class InterestServiceTest extends IntegrationTestSupport {

  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private KeywordRepository keywordRepository;
  @Autowired
  private InterestKeywordRepository interestKeywordRepository;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private InterestService interestService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private NewsArticleRepository newsArticleRepository;
  @Autowired
  private ArticleInterestRepository articleInterestRepository;

  @Transactional
  @DisplayName("관심사와 키워드를 입력할 경우, 관심사를 등록할 수 있습니다.")
  @Test
  void registerInterest() {
    // given
    String interestName = UUID.randomUUID().toString();
    String keyword = UUID.randomUUID().toString();
    interestRepository.save(new Interest(UUID.randomUUID().toString()));
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest(interestName,
        List.of(keyword));

    // when
    InterestResult interestResult = interestService.registerInterest(interestRegisterRequest);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(interestResult)
          .extracting(InterestResult::interestName, InterestResult::subscriberCount,
              InterestResult::subscribedByMe, InterestResult::keywords)
          .containsExactlyInAnyOrder(interestName, 0, null, List.of(keyword));
    });
  }

  @Transactional
  @DisplayName("관심사와 키워드로 조회하면, 키워드를 포함하는 데이터를 반환합니다.")
  @MethodSource("provideInterestSearchArguments")
  @ParameterizedTest
  void getInterests_SearchInterestAndKeyWord_Parameterized(int limit, boolean expectedHasNext,
      String expectedCursorInterestName, List<String> expectedNames) {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    Interest savedNextInterest = saveInterestAndKeyword("러닝", List.of("한강"));
    Map<String, Interest> nameToInterest = Map.of(
        savedInterest.getName(), savedInterest,
        savedNextInterest.getName(), savedNextInterest
    );

    InterestSearchRequest interestSearchRequest = new InterestSearchRequest(
        "러닝",
        "name",
        "DESC",
        null,
        null,
        limit
    );

    // when
    CursorPageInterestResponse<InterestResult> interests = interestService.getInterests(
        interestSearchRequest, savedUser.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(interests.hasNext()).isEqualTo(expectedHasNext);
      softly.assertThat(interests.totalElements()).isEqualTo(2);
      softly.assertThat(interests.nextCursor())
          .isEqualTo(nameToInterest.get(expectedCursorInterestName).getName());
      softly.assertThat(interests.nextAfter())
          .isEqualTo(nameToInterest.get(expectedCursorInterestName).getCreatedAt().toString());
      softly.assertThat(interests.contents())
          .extracting(InterestResult::interestName)
          .containsExactlyElementsOf(expectedNames);
    });
  }

  private static Stream<Arguments> provideInterestSearchArguments() {
    return Stream.of(
        Arguments.of(2, false, "러닝", List.of("러닝머신", "러닝")),
        Arguments.of(1, true, "러닝머신", List.of("러닝머신"))
    );
  }

  @Transactional
  @DisplayName("사용자가 관심사를 구독합니다.")
  @Test
  void subscribeInterest() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

    // when
    SubscriptionResult subscriptionResult = interestService.subscribeInterest(savedInterest.getId(),
        savedUser.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(subscriptionResult)
          .extracting(SubscriptionResult::interestId, SubscriptionResult::interestKeyword,
              SubscriptionResult::interestSubscriberCount)
          .containsExactlyInAnyOrder(savedInterest.getId(), List.of("중랑천"), 1);
      softly.assertThat(subscriptionResult.id())
          .isEqualTo(new SubscriptionId(savedInterest.getId(), savedUser.getId()));
    });
  }

  @Transactional
  @DisplayName("등록되지 않은 사용자가 관심사를 구독하면, 예외를 반환합니다.")
  @Test
  void subscribeInterest_NoUserException() {
    // given
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

    // when & then
    Assertions.assertThatThrownBy(
            () -> interestService.subscribeInterest(savedInterest.getId(), -1L))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Transactional
  @DisplayName("등록되지 않은 관심사를 구독한다면, 예외를 반환합니다.")
  @Test
  void subscribeInterest_NoInterestException() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));

    // when & then
    Assertions.assertThatThrownBy(() -> interestService.subscribeInterest(-1L, savedUser.getId()))
        .isInstanceOf(InterestNotFoundException.class);
  }

  @Transactional
  @DisplayName("관심사 구독을 취소합니다.")
  @Test
  void unsubscribeInterest() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    SubscriptionResult subscriptionResult = interestService.subscribeInterest(savedInterest.getId(),
        savedUser.getId());

    // when
    interestService.unsubscribeInterest(savedInterest.getId(), savedUser.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(subscriptionRepository.findById(
              new SubscriptionId(savedInterest.getId(), savedUser.getId())))
          .isEmpty();
      softly.assertThat(interestRepository.findById(savedInterest.getId()))
          .isPresent()
          .get()
          .extracting(Interest::getSubscriberCount)
          .isEqualTo(0);
    });
  }

  @Transactional
  @DisplayName("관심사 구독을 취소할때, 유저가 없으면 예외를 반환합니다.")
  @Test
  void unsubscribeInterest_NoUser() {
    // given
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));

    // when & then
    Assertions.assertThatThrownBy(
            () -> interestService.unsubscribeInterest(savedInterest.getId(), -1L))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Transactional
  @DisplayName("관심사 구독을 취소할떄, 관심사가 없으면 예외를 반환합니다.")
  @Test
  void unsubscribeInterest_NoInterest() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));

    // when & then
    Assertions.assertThatThrownBy(() -> interestService.unsubscribeInterest(-1L, savedUser.getId()))
        .isInstanceOf(InterestNotFoundException.class);
  }

  @Transactional
  @DisplayName("관심사를 삭제합니다.")
  @Test
  void deleteInterest() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    NewsArticle newsArticle = saveNewsArticle("hello");
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    articleInterestRepository.save(
        new ArticleInterest(new ArticleInterestId(newsArticle.getId(), savedInterest.getId())));
    interestService.subscribeInterest(savedInterest.getId(), savedUser.getId());

    // when
    interestService.deleteInterest(savedInterest.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(interestRepository.findById(savedInterest.getId())).isEmpty();
      softly.assertThat(interestKeywordRepository.findByInterest_Id(savedInterest.getId()))
          .isEmpty();
      softly.assertThat(subscriptionRepository.findById(
          new SubscriptionId(savedInterest.getId(), savedUser.getId()))).isEmpty();
    });
  }

  @Transactional
  @DisplayName("관심사를 삭제할때, 관심사가 없으면 예외를 반환합니다.")
  @Test
  void deleteInterest_NoInterestException() {
    // when & then
    Assertions.assertThatThrownBy(() -> interestService.deleteInterest(-1L))
        .isInstanceOf(InterestNotFoundException.class);
  }

  @Transactional
  @DisplayName("관심사의 키워드 정보를 수정합니다.")
  @Test
  void updateInterest() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("중랑천", "면목천"));

    // when
    InterestResult interestResult = interestService.updateKeywordInInterest(savedInterest.getId(),
        savedUser.getId(), interestUpdateRequest);

    // then
    Assertions.assertThat(interestResult)
        .extracting(InterestResult::interestName, InterestResult::keywords,
            InterestResult::subscribedByMe)
        .containsExactlyInAnyOrder("러닝머신", List.of("중랑천", "면목천"), false);
  }

  @Transactional
  @DisplayName("관심사의 키워드 정보를 수정할떄, 관심사가 없으면 예외를 반환합니다.")
  @Test
  void updateInterest_NoInterestException() {
    // given
    User savedUser = userRepository.save(new User("", "", ""));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("중랑천", "면목천"));

    // when & then
    Assertions.assertThatThrownBy(
            () -> interestService.updateKeywordInInterest(-1L, savedUser.getId(),
                interestUpdateRequest))
        .isInstanceOf(InterestNotFoundException.class);
  }

  @Transactional
  @DisplayName("관심사의 키워드 정보를 수정할떄, 등록되지 않은 유저면 예외를 반환합니다.")
  @Test
  void updateInterest_NoUserException() {
    // given
    Interest savedInterest = saveInterestAndKeyword("러닝머신", List.of("중랑천"));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("중랑천", "면목천"));

    // when & then
    Assertions.assertThatThrownBy(
            () -> interestService.updateKeywordInInterest(savedInterest.getId(), -1L,
                interestUpdateRequest))
        .isInstanceOf(UserNotFoundException.class);
  }

  private Interest saveInterestAndKeyword(String interestName, List<String> keywordNames) {
    Interest savedNextInterest = interestRepository.save(new Interest(interestName));
    List<Keyword> keywords = keywordNames.stream().map(Keyword::new).toList();
    List<Keyword> savedNextKeywords = keywordRepository.saveAll(keywords);
    List<InterestKeyword> nextInterestKeywords = savedNextKeywords.stream()
        .map(keyword -> new InterestKeyword(savedNextInterest, keyword))
        .toList();
    interestKeywordRepository.saveAll(nextInterestKeywords);

    return savedNextInterest;
  }

  @NotNull
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