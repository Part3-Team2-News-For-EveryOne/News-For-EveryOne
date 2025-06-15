package com.example.newsforeveryone.newsarticle.batch.writer;

import com.example.newsforeveryone.newsarticle.entity.ArticleInterest;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.ArticleInterestRepository;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.notification.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class ArticleItemWriter implements ItemWriter<NewsArticle> {

  private final NewsArticleRepository newsArticleRepository;
  private final ArticleInterestRepository articleInterestRepository;
  private final NotificationService notificationService;

  @Override
  public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
    List<NewsArticle> newArticles = filterNewArticles(chunk);
    if (newArticles.isEmpty()) {
      return;
    }

    List<NewsArticle> savedArticles = newsArticleRepository.saveAll(newArticles);

    saveArticleInterestsOf(savedArticles);
  }

  private void saveArticleInterestsOf(List<NewsArticle> savedArticles) {
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
    if (!articleInterestsToSave.isEmpty()) {
      articleInterestRepository.saveAll(articleInterestsToSave);
    }
  }

  private List<NewsArticle> filterNewArticles(Chunk<? extends NewsArticle> chunk) {
    List<? extends NewsArticle> items = chunk.getItems();
    if (items.isEmpty()) {
      return List.of();
    }

    Set<String> linksInChunk = items.stream()
        .map(NewsArticle::getLink)
        .collect(Collectors.toSet());
    Set<String> existingLinks = Set.copyOf(newsArticleRepository.findLinksByLinkIn(linksInChunk));

    return items.stream()
        .filter(article -> !existingLinks.contains(article.getLink()))
        .map(article -> (NewsArticle) article)
        .toList();
  }
}

