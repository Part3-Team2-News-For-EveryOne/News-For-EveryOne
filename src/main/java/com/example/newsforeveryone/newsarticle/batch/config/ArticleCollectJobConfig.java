package com.example.newsforeveryone.newsarticle.batch.config;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.dto.ArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ArticleCollectJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  
  @Bean
  public Job articleCollectJob(Step collectRssStep) {
    return new JobBuilder("articleCollectJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(collectRssStep)
        .build();
  }
  
  @Bean
  public Step collectRswsStep(
      ItemReader<RssRawArticleDto> reader, //ArticleDto로 읽어와지는건가?
      ItemProcessor<RssRawArticleDto, NewsArticle> processor,
      ItemWriter<NewsArticle> writer
  ) {
    return new StepBuilder("collectRssStep", jobRepository)
        .<RssRawArticleDto, NewsArticle>chunk(50, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .build();
  }
}
