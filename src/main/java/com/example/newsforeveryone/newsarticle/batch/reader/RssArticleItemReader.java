package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.parser.RssParser;
import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RssArticleItemReader implements ItemReader<RssRawArticleDto> {

  // 조선일보 정치 https://www.chosun.com/arc/outboundfeeds/rss/category/politics/?outputType=xml

  // 아마도 필요한 Rss파서들을 가지고 있어야 함 List<RssParser>
  private final RssParser rssParser;

  private Iterator<RssRawArticleDto> iterator;

  @PostConstruct
  public void init() {
    List<RssRawArticleDto> articles = fetchRss(); // RSS 수집 로직
    this.iterator = articles.iterator();
  }

  @Override
  public RssRawArticleDto read() {
    return iterator.hasNext() ? iterator.next() : null;
  }


  // 상엽
  private List<RssRawArticleDto> fetchRss() {
    // URL 읽기 -> 알맞은 파서가 있으면 파싱 → DTO 리스트로 변환
    String RssUrl = "https://www.chosun.com/arc/outboundfeeds/rss/category/politics/?outputType=xml";
    return rssParser.parse(RssUrl);

  }
}

