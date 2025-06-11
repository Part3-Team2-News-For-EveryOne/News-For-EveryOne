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

  RssRawArticleDto mapItem(Element element);

  default List<RssRawArticleDto> parse(String feedUrl, RestTemplate restTemplate){
    try{
      Document document = fetchDocument(feedUrl, restTemplate);
      NodeList items = document.getElementsByTagName("item");
      return parseDocument(document);
    } catch(Exception ex){
      ex.printStackTrace();
      return Collections.emptyList();
    }
  }

  // catch문을 대신 함
  @SneakyThrows
  // xml파일에서 document생성
  default Document fetchDocument(String feedUrl, RestTemplate restTemplate) {
    Resource resource = restTemplate.getForObject(feedUrl, Resource.class);
    if(resource == null){
      throw new IllegalArgumentException("Feed를 가져올 수 없습니다: "+ feedUrl);
    }

    try(InputStream is = resource.getInputStream()){
      return DocumentBuilderFactory
          .newInstance()
          .newDocumentBuilder()
          .parse(is);
    }
  }

  default List<RssRawArticleDto> parseDocument(Document doc) {
    doc.getDocumentElement().normalize();
    NodeList items = doc.getElementsByTagName("item");
    List<RssRawArticleDto> result = new ArrayList<>();

    for (int i = 0; i < items.getLength(); i++) {
      Element item = (Element) items.item(i);
      RssRawArticleDto dto = mapItem(item);
      if (dto != null) {
        result.add(dto);
      }
    }
    return result;
  }

  default String getText(Element parent, String tagName) {
    NodeList list = parent.getElementsByTagName(tagName);
    if (list.getLength() == 0) return null;
    String txt = list.item(0).getTextContent();
    return (txt != null) ? txt.trim() : null;
  }

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
