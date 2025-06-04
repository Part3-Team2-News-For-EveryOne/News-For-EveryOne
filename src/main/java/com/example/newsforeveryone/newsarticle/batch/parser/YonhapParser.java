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

// 본문 내용이 조선은 content:encoded에 포함되어있고,
// 연합은 description에 요약이 포함되어있음
@Component
@RequiredArgsConstructor
public class YonhapParser implements RssParser{
  private final RestTemplate restTemplate;

  @Override
  public boolean supports(String feedUrl) {
    return feedUrl.contains("yonhapnewstv.co.kr");
  }

  @Override
  public List<RssRawArticleDto> parse(String url) {
    try {
      // RSS XML -> Resource 형태로 가져오기
      Resource resource = restTemplate.getForObject(url, Resource.class);
      if (resource == null) return List.of();

      // InputStream에서 document parsing
      InputStream stream = resource.getInputStream();
      Document doc = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .parse(stream);
      doc.getDocumentElement().normalize();;

      // item nodelist 가져옴
      NodeList items = doc.getElementsByTagName("item");
      List<RssRawArticleDto> articles = new ArrayList<>();

      for (int i = 0; i < items.getLength(); i++) {
        Element item = (Element) items.item(i);

        String title = getText(item, "title");
        String link = getText(item, "link");
        // 연합뉴스는 description이 요약
        String summary = getText(item, "description");
        String author = getText(item, "dc:creator");
        Instant publishedAt = parseDate(getText(item, "pubDate"));

        articles.add(new RssRawArticleDto(
            "yonhap", link, title, summary, author, publishedAt
        ));
      }

      return articles;
    } catch (Exception e) {
      e.printStackTrace();
      return List.of();
    }
  }

  private String getText(Element parent, String tagName) {
    NodeList list = parent.getElementsByTagName(tagName);
    if (list.getLength() == 0) return null;
    String text = list.item(0).getTextContent();
    return (text != null) ? text.trim() : null;
  }

  // parsing 실패시 현재 시각으로 대체
  private Instant parseDate(String raw) {
    if(raw == null || raw.isEmpty()){
      return Instant.now();
    }
    try {
      return ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    } catch (Exception e) {
      return Instant.now(); // fallback
    }
  }
}
