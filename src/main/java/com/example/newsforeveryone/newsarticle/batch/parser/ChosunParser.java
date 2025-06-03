package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class ChosunParser implements RssParser {
  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public List<RssRawArticleDto> parse(String url) {
    try {
      Resource resource = restTemplate.getForObject(url, Resource.class);
      if (resource == null) return List.of();

      InputStream stream = resource.getInputStream();

      Document doc = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .parse(stream);
      doc.getDocumentElement().normalize();;

      NodeList items = doc.getElementsByTagName("item");
      List<RssRawArticleDto> articles = new ArrayList<>();

      for (int i = 0; i < items.getLength(); i++) {
        Element item = (Element) items.item(i);

        String title = getText(item, "title");
        String link = getText(item, "link");
//        String description = getText(item, "content:encoded");

        String html = getText(item, "content:encoded");

        int start = html.indexOf("<p>");
        int end = html.indexOf("</p>");
        String description = (start != -1 && end != -1) ? html.substring(start + 3, end) : "";

        String author = getText(item, "dc:creator");
        Instant publishedAt = parseDate(getText(item, "pubDate"));

        articles.add(new RssRawArticleDto(
            "chosun", link, title, description, author, publishedAt
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
    if (list.getLength() == 0 || list.item(0).getTextContent() == null) return null;
    return list.item(0).getTextContent().trim();
  }

  private Instant parseDate(String raw) {
    try {
      return ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    } catch (Exception e) {
      return Instant.now(); // fallback
    }
  }

}
