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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// 연합은 description에 요약이 포함되어있음
@Slf4j
@Component
@RequiredArgsConstructor
public class YonhapParser implements RssParser{
  private final RestTemplate restTemplate;

  @Override
  public boolean supports(String feedUrl) {
    return feedUrl.contains("yonhapnewstv.co.kr");
  }

  @Override
  public RssRawArticleDto mapItem(Element item) {
    String title = getText(item, "title");
    String link = getText(item, "link");
    // 연합뉴스는 description이 요약
    String summary = getText(item, "description");
    String author = getText(item, "dc:creator");
    Instant publishedAt = parseDate(getText(item, "pubDate"));


    return new RssRawArticleDto(
        "yonhap",
        link,
        title,
        summary,
        author,
        publishedAt
    );
  }
}
