package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.interest.repository.InterestKeywordRepository;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArticleProcessorConfig {

  @Bean
  @JobScope
  public KeywordInterestCache keywordInterestCache(InterestKeywordRepository repo) {
    KeywordInterestCache cache = new KeywordInterestCache(repo);
    cache.init();
    return cache;
  }

  @Bean
  @StepScope
  public ArticleProcessor articleProcessor(KeywordInterestCache cache) {
    return new ArticleProcessor(cache);
  }
}
