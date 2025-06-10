package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssParserRegistry {

  private final List<RssParser> parsers;

  public RssParser getParser(String feedUrl) {
    return parsers.stream()
        .filter(parser -> parser.supports(feedUrl))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 RSS URL: " + feedUrl));
  }
}

