package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<RssRawArticleDto, NewsArticle> {

  private final KeywordInterestCache keywordInterestCache;

  @Override
  public NewsArticle process(RssRawArticleDto item) {
    String content = item.title() + " " + item.summary();
    Set<Long> matchedInterestIds = keywordInterestCache.findInterestIdsFromContent(content);

//    // 매칭된 관심사가 없다면 null return
//    if(matchedInterestIds.isEmpty()) return null;

    return NewsArticle.builder()
        .sourceName(item.sourceName())
        .title(item.title())
        .link(item.link())
        .summary(item.summary())
        .publishedAt(item.publishedAt())
        .interestIds(matchedInterestIds)
        .build();
  }


}