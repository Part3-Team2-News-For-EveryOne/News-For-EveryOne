package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.SneakyThrows;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public interface RssParser {

  boolean supports(String feedUrl);

  // item 태그 하나를 받아서 RssRawArticleDto로 변환
  RssRawArticleDto mapItem(Element element);

  // 외부에서 호출
  // Http를 통해 feedUrl을 가져온 뒤 Document로 파싱
  // Document
  default List<RssRawArticleDto> parse(String feedUrl, RestTemplate restTemplate){
    try{
      Document document = fetchDocument(feedUrl, restTemplate);
      NodeList items = document.getElementsByTagName("item");
      System.out.println("Yonhap feed = " + feedUrl + " → root element = " + document.getDocumentElement().getNodeName());

      return parseDocument(document);
    } catch(Exception ex){
      ex.printStackTrace();
      return Collections.emptyList();
    }
  }

  // restTemplate로 URL을 호출해서 Resource 얻고, InputStream을 document로 parsing
  @SneakyThrows
  default Document fetchDocument(String feedUrl, RestTemplate restTemplate) {
    Resource resource = restTemplate.getForObject(feedUrl, Resource.class);
    if(resource == null){
      throw new IllegalArgumentException("Feed를 가져올 수 없습니다: "+ feedUrl);
    }

    // documentBuilderFactory 설정
    try(InputStream is = resource.getInputStream()){
      return DocumentBuilderFactory
          .newInstance()
          .newDocumentBuilder()
          .parse(is);
    }
  }

  // Document에서 <item> 노드만 꺼내서, mapItem(구현체) 호출 → DTO 리스트 반환
  default List<RssRawArticleDto> parseDocument(Document doc) {
    doc.getDocumentElement().normalize();
    NodeList items = doc.getElementsByTagName("item");
    List<RssRawArticleDto> result = new ArrayList<>();

    for (int i = 0; i < items.getLength(); i++) {
      Element item = (Element) items.item(i);

      // mapItem 메서드는 각 parser에서 각 rss에 구조에 따라 다르게 구현
      RssRawArticleDto dto = mapItem(item);

      if (dto != null) {
        result.add(dto);
      }
    }
    return result;
  }

  // Element 내부의 텍스트 추출
  default String getText(Element parent, String tagName) {
    NodeList list = parent.getElementsByTagName(tagName);
    if (list.getLength() == 0) return null;
    String txt = list.item(0).getTextContent();
    return (txt != null) ? txt.trim() : null;
  }

  // RFC_1123 포맷으로 받은 pubDate 문자열을 Instant로 변환
  // parsing 실패 시 현재 시각으로 fallback
  default Instant parseDate(String raw) {
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
