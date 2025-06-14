package com.example.newsforeveryone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class NewsForEveryOneApplication {

  public static void main(String[] args) {
    SpringApplication.run(NewsForEveryOneApplication.class, args);
  }

}
