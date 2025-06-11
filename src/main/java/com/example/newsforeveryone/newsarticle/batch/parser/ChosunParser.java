package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@RequiredArgsConstructor
public class ChosunParser implements RssParser {
  private final RestTemplate restTemplate;

  @Override
  public boolean supports(String feedUrl) {
    return feedUrl.contains("www.chosun.com");
  }

  @Override
  public RssRawArticleDto mapItem(Element item) {
    String title = getText(item, "title");
    String link = getText(item, "link");
    String html = getText(item, "content:encoded");

    String description = "";
    if(html != null) {
      int start = html.indexOf("<p>");
      int end = html.indexOf("</p>");
      if(start != -1 && end != -1)
        description = (start != -1 && end != -1) ? html.substring(start + 3, end) : "";
    }

    String author = getText(item, "dc:creator");
    Instant publishedAt = parseDate(getText(item, "pubDate"));

    return new RssRawArticleDto(
        "조선RSS",
        link,
        title,
        description,
        author,
        publishedAt
    );
  }
}
