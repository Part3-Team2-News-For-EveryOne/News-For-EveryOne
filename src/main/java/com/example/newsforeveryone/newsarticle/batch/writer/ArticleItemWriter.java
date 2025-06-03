package com.example.newsforeveryone.newsarticle.batch.writer;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleItemWriter implements ItemWriter<NewsArticle> {

  private final NewsArticleRepository repository;

  @Override
  public void write(Chunk<? extends NewsArticle> items) throws Exception {
    repository.saveAll(items);
  }
}

