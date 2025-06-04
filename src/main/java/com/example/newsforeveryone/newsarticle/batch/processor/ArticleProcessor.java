package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ArticleProcessor implements ItemProcessor<RssRawArticleDto, NewsArticle> {

  @Override
  public NewsArticle process(RssRawArticleDto item) {
    return NewsArticle.builder()
        .sourceName(item.sourceName())
        .title(item.title())
        .link(item.link())
        .summary(item.summary())
        .publishedAt(item.publishedAt())
        .build();
  }
}