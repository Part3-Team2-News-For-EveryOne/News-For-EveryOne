package com.example.newsforeveryone.newsarticle.batch.config;

import com.example.newsforeveryone.newsarticle.batch.dto.ArticleItemNormalizer;
import com.example.newsforeveryone.newsarticle.batch.dto.ChosunRssItemDto;
import com.example.newsforeveryone.newsarticle.batch.dto.HankyungRssItemDto;
import com.example.newsforeveryone.newsarticle.batch.dto.NaverItemDto;
import com.example.newsforeveryone.newsarticle.batch.dto.YonhapRssItemDto;
import com.example.newsforeveryone.newsarticle.batch.tasklet.NotificationTasklet;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ArticleCollectJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Qualifier("chosunItemReader")
  private final ItemReader<ChosunRssItemDto> chosunItemReader;

  @Qualifier("hankyungItemReader")
  private final ItemReader<HankyungRssItemDto> hankyungItemReader;

  @Qualifier("yonhapItemReader")
  private final ItemReader<YonhapRssItemDto> yonhapItemReader;

  @Qualifier("naverItemReader")
  private final ItemReader<NaverItemDto> naverItemReader;

  @Qualifier("articleProcessor")
  private final ItemProcessor<ArticleItemNormalizer, NewsArticle> articleProcessor;

  @Qualifier("articleItemWriter")
  private final ItemWriter<NewsArticle> articleItemWriter;

  private final NotificationTasklet notificationTasklet;

  @Bean
  public Job articleCollectJob() {
    return new JobBuilder("articleCollectJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(collectChosunStep())
        .next(collectHankyungStep())
        .next(collectYonhapStep())
        .next(collectNaverStep())
        .next(notificationStep())
        .build();
  }

  @Bean
  public Step collectChosunStep() {
    return createArticleCollectionStep("collectChosunStep", chosunItemReader);
  }

  @Bean
  public Step collectHankyungStep() {
    return createArticleCollectionStep("collectHankyungStep", hankyungItemReader);
  }

  @Bean
  public Step collectYonhapStep() {
    return createArticleCollectionStep("collectYonhapStep", yonhapItemReader);
  }

  @Bean
  public Step collectNaverStep() {
    return createArticleCollectionStep("collectNaverStep", naverItemReader);
  }

  private Step createArticleCollectionStep(String stepName, ItemReader<? extends ArticleItemNormalizer> reader) {
    return new StepBuilder(stepName, jobRepository)
        .<ArticleItemNormalizer, NewsArticle>chunk(50, transactionManager)
        .reader(reader)
        .processor(articleProcessor)
        .writer(articleItemWriter)
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skipLimit(100)
        .build();
  }

  @Bean
  public Step notificationStep() {
    return new StepBuilder("notificationStep", jobRepository)
        .tasklet(notificationTasklet, transactionManager)
        .build();
  }
}
