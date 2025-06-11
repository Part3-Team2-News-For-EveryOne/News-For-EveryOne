package com.example.newsforeveryone.newsarticle.batch.config;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ArticleBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NewsArticleRepository newsArticleRepository;


    @Bean
    public Job backupNewsArticleJob(Step backupNewsArticleStep) {
        return new JobBuilder("backupNewsArticleJob", jobRepository)
                .start(backupNewsArticleStep)
                .build();
    }

    @Qualifier("backupNewsArticleStep")
    @Bean
    public Step backupNewsArticleStep(
            @Qualifier("ArticleBackupWriter") ItemWriter<NewsArticle> newsArticleBackupWriter
    ) {
        return new StepBuilder("backupNewsArticleStep", jobRepository)
                .<NewsArticle, NewsArticle>chunk(10, transactionManager)
                .reader(newsArticleReader())
                .writer(newsArticleBackupWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<NewsArticle> newsArticleReader() {
        Instant start = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<NewsArticle> articles = newsArticleRepository.findAllByCreatedAtBetween(start, end);
        return new ListItemReader<>(articles);
    }
}
