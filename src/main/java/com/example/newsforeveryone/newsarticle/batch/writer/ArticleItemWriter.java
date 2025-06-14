package com.example.newsforeveryone.newsarticle.batch.writer;

import com.example.newsforeveryone.newsarticle.entity.ArticleInterest;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.ArticleInterestRepository;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.notification.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("ArticleItemWriter")
@RequiredArgsConstructor
public class ArticleItemWriter implements ItemWriter<NewsArticle> {

  private final NewsArticleRepository newsArticleRepository;
  private final ArticleInterestRepository articleInterestRepository;
  private final NotificationService notificationService;

  @Override
  public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
    List<? extends NewsArticle> items = chunk.getItems();

    List<NewsArticle> articlesToSave = new ArrayList<>(items);
    List<NewsArticle> savedArticles = newsArticleRepository.saveAll(articlesToSave);

    List<ArticleInterest> articleInterestsToSave = new ArrayList<>();
    for (NewsArticle savedArticle : savedArticles) {
      if (savedArticle.getInterestIds() == null) {
        continue;
      }
      for (Long interestId : savedArticle.getInterestIds()) {
        articleInterestsToSave.add(
            new ArticleInterest(new ArticleInterestId(savedArticle.getId(), interestId)));
      }
    }
    List<ArticleInterest> articleInterests = articleInterestRepository.saveAll(
        articleInterestsToSave);
    sendNotification(articleInterests);
  }

  private void sendNotification(List<ArticleInterest> articleInterests) {
    List<ArticleInterestId> articleInterestIds = articleInterests.stream()
        .map(ArticleInterest::getId)
        .toList();
    notificationService.createNotificationByInterest(articleInterestIds);
  }

}

