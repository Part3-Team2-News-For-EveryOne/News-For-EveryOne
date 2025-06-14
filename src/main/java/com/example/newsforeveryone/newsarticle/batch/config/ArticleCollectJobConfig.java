package com.example.newsforeveryone.newsarticle.batch.config;

import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.tasklet.NotificationTasklet;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ArticleCollectJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Qualifier("rssReader")
  private final ItemReader<RawArticleDto> rssReader;

  @Qualifier("naverReader")
  private final ItemReader<RawArticleDto> naverReader;

  private final ItemProcessor<RawArticleDto, NewsArticle> processor;

  private final NotificationTasklet notificationTasklet;

  @Qualifier("ArticleItemWriter")
  private final ItemWriter<NewsArticle> writer;

  @Bean
  public Job articleCollectJob() {
    JobBuilder builder = new JobBuilder("articleCollectJob", jobRepository);
    return builder
        .incrementer(new RunIdIncrementer())
        .start(collectRssStep())
        .next(collectNaverStep())
        .next(notificationStep())
        .build();
  }

  @Qualifier("collectRssStep")
  @Bean
  public Step collectRssStep() {
    StepBuilder builder = new StepBuilder("collectRssStep", jobRepository);
    SimpleStepBuilder<RawArticleDto, NewsArticle> step = builder
        .<RawArticleDto, NewsArticle>chunk(50, transactionManager)
        .reader(rssReader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skip(DataIntegrityViolationException.class);

    return step.build();
  }

  @Bean
  public Step collectNaverStep() {
    StepBuilder builder = new StepBuilder("collectNaverStep", jobRepository);
    SimpleStepBuilder<RawArticleDto, NewsArticle> step = builder
        .<RawArticleDto, NewsArticle>chunk(50, transactionManager)
        .reader(naverReader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skip(DataIntegrityViolationException.class);

    return step.build();
  }

  @Bean
  public Step notificationStep() {
    return new StepBuilder("notificationStep", jobRepository)
        .tasklet(notificationTasklet, transactionManager)
        .build();
  }
}
