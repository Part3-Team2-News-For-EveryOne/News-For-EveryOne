package com.example.newsforeveryone.newsarticle.batch.tasklet;

import com.example.newsforeveryone.newsarticle.repository.ArticleInterestRepository;
import com.example.newsforeveryone.notification.service.NotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import com.example.newsforeveryone.newsarticle.entity.ArticleInterestId;


@Component
@StepScope
@RequiredArgsConstructor
public class NotificationTasklet implements Tasklet {

  private final NotificationService notificationService;
  private final ArticleInterestRepository articleInterestRepository;

  @Value("#{jobParameters['requestTime']}")
  private String requestTime;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    Instant since = Instant.parse(requestTime);
    List<ArticleInterestId> articleInterestIds = articleInterestRepository.findIdsByCreatedAtAfter(since);

    if (!articleInterestIds.isEmpty()){
      notificationService.createNotificationByInterest(articleInterestIds);
      System.out.println("Total " + articleInterestIds.size() + " interests processed for notification.");
    }

    return RepeatStatus.FINISHED;
  }
}