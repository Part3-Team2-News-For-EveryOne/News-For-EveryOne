package com.example.newsforeveryone.newsarticle.batch.scheduler;

import com.example.newsforeveryone.newsarticle.entity.Source;
import com.example.newsforeveryone.newsarticle.repository.SourceRepository;
import java.time.Instant;
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

  private final SourceRepository sourceRepository;

  @Scheduled(cron = "0 0/20 * * * *")
  public void runJob() throws Exception {
    String chosunUrl = getFeedUrlOrDefault("조선RSS", "");
    String hankyungUrl = getFeedUrlOrDefault("한경RSS", "");
    String yonhapUrl = getFeedUrlOrDefault("연합RSS", "");


    JobParameters params = new JobParametersBuilder()
        .addLong("run.id", System.currentTimeMillis())
        .addString("requestTime", Instant.now().toString())
        .addString("chosunUrl", chosunUrl)
        .addString("hankyungUrl", hankyungUrl)
        .addString("yonhapUrl", yonhapUrl)
        .toJobParameters();
    jobLauncher.run(articleCollectJob, params);
  }

  private String getFeedUrlOrDefault(String sourceName, String defaultString) {
    return sourceRepository.findByName(sourceName)
        .map(Source::getFeedUrl)
        .orElse(defaultString);
  }

  @Scheduled(cron = "0 50 23 * * *")
  public void runBackupJob() throws Exception {
    JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();
    jobLauncher.run(backupNewsArticleJob, params);
  }
}
