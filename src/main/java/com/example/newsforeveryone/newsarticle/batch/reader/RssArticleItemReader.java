package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.parser.RssParser;
import com.example.newsforeveryone.newsarticle.batch.parser.RssParserRegistry;
import com.example.newsforeveryone.newsarticle.repository.NewsArticleRepository;
import com.example.newsforeveryone.newsarticle.repository.SourceRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RssArticleItemReader implements ItemReader<RssRawArticleDto> {

  private final SourceRepository sourceRepository;
  private final RssParserRegistry parserRegistry;
  private final NewsArticleRepository newsArticleRepository;
  // 새로 추가
  private final RestTemplate restTemplate;
  private Iterator<RssRawArticleDto> iterator;

  @BeforeStep
  public void beforeStep(StepExecution stepExecution) {
    List<RssRawArticleDto> articles = fetchRss();
    this.iterator = articles.iterator();
  }

  @Override
  public RssRawArticleDto read() {
    return iterator.hasNext() ? iterator.next() : null;
  }


  private List<RssRawArticleDto> fetchRss() {
    List<String> feedUrls = sourceRepository.findAllFeedUrl()
        .orElseThrow(() -> new IllegalArgumentException("RSS URL이 존재하지 않습니다."));
    // log
    feedUrls.forEach(url -> log.info("Loaded feedUrl: {}", url));

    List<RssRawArticleDto> allArticles = new ArrayList<>();

    for (String feedUrl : feedUrls) {
      try {
        RssParser parser = parserRegistry.getParser(feedUrl);
        List<RssRawArticleDto> articleDtos = parser.parse(feedUrl, restTemplate);
        allArticles.addAll(articleDtos);
      } catch (Exception e) {
        throw new RuntimeException("Rss Parsing 중 오류 발생: " + feedUrl, e);
      }
    }
    return filterDuplicatedArticles(allArticles);
  }

  private List<RssRawArticleDto> filterDuplicatedArticles(List<RssRawArticleDto> unfilteredArticles) {
    if (unfilteredArticles.isEmpty()) {
      return new ArrayList<>();
    }

    Set<String> unfilteredArticleLinks = unfilteredArticles.stream()
        .map(RssRawArticleDto::link)
        .collect(Collectors.toSet());

    Set<String> existingLinks = new HashSet<>(newsArticleRepository.findLinksByLinkIn(unfilteredArticleLinks));

    List<RssRawArticleDto> filteredArticles = unfilteredArticles.stream()
        .filter(article -> !existingLinks.contains(article.link()))
        .distinct()
        .toList();

    return filteredArticles;
  }
}

