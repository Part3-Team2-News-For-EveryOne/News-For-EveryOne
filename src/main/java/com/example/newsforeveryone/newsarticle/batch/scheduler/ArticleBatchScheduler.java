package com.example.newsforeveryone.newsarticle.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ArticleBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job articleCollectJob;
  private final Job backupNewsArticleJob;

  @Scheduled(cron = "0 0 * * * *") // 매 정시
  public void runJob() throws Exception {
    JobParameters params = new JobParametersBuilder()
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();
    jobLauncher.run(articleCollectJob, params);
  }

  @Scheduled(cron = "0 50 23 * * *")
  public void runBackupJob() throws Exception {
    JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();
    jobLauncher.run(backupNewsArticleJob, params);
  }
}
