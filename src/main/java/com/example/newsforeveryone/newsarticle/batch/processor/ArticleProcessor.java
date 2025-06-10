package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<RawArticleDto, NewsArticle> {

  private final KeywordInterestCache keywordInterestCache;

  @Override
  public NewsArticle process(RawArticleDto item) {
    String content = item.title() + " " + item.description();
    Set<Long> matchedInterestIds = keywordInterestCache.findInterestIdsFromContent(content);

    return NewsArticle.builder()
        .sourceName(item.sourceName())
        .title(item.title())
        .link(item.link())
        .summary(item.description())
        .publishedAt(item.publishedAt())
        .interestIds(matchedInterestIds)
        .build();
  }
}