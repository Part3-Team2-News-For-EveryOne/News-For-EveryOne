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
  public List<RssRawArticleDto> parse(String url) {
    try {
      // feed URL로부터 Resource 가져오기
      Resource resource = restTemplate.getForObject(url, Resource.class);
      if (resource == null) {
        return List.of();
      }

      // InputStream → DOM Document 파싱
      InputStream stream = resource.getInputStream();
      Document doc = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .parse(stream);
      doc.getDocumentElement().normalize();

      // <item> 노드 리스트 획득
      NodeList items = doc.getElementsByTagName("item");
      List<RssRawArticleDto> articles = new ArrayList<>();

      for (int i = 0; i < items.getLength(); i++) {
        Element item = (Element) items.item(i);

        // 각 태그에서 텍스트 추출
        String title = getText(item, "title");
        String link = getText(item, "link");
        String author = getText(item, "author");
        Instant publishedAt = parseDate(getText(item, "pubDate"));

        // 한국경제 RSS에는 별도의 description(요약)이 제공되지 않으므로 빈 문자열로 설정
        String summary = title;

        // DTO 생성 (sourceName을 "hankyung"으로 고정)
        articles.add(new RssRawArticleDto(
            "hankyung",
            link,
            title,
            summary,
            author,
            publishedAt
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
    if (list.getLength() == 0 || list.item(0).getTextContent() == null) {
      return null;
    }
    return list.item(0).getTextContent().trim();
  }

  private Instant parseDate(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Instant.now();
    }
    try {
      return ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    } catch (Exception e) {
      return Instant.now();
    }
  }
}