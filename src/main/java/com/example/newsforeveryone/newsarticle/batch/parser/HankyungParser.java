package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@RequiredArgsConstructor
public class HankyungParser implements RssParser {

  private final RestTemplate restTemplate;

  @Override
  public boolean supports(String feedUrl) {
    // 한국경제 RSS URL에 "hankyung.com"이 포함되어 있으면 이 파서를 사용
    return feedUrl.contains("hankyung.com");
  }

  @Override
  public RssRawArticleDto mapItem(Element item) {
    String title = getText(item, "title");
    String link = getText(item, "link");
    String author = getText(item, "author");
    Instant publishedAt = parseDate(getText(item, "pubDate"));

    // 한국경제 RSS에는 별도의 description(요약)이 제공되지 않으므로 title 주입
    String summary = title;

    return new RssRawArticleDto(
        "hankyung",
        link,
        title,
        summary,
        author,
        publishedAt
    );
  }
}