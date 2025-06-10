package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<RawArticleDto, NewsArticle> {

  private final KeywordInterestCache keywordInterestCache;

  @Override
  public NewsArticle process(RawArticleDto item) {
      String cleanedSummary = cleanHtmlContent(item.description());

      if (cleanedSummary.isBlank()) {
          return null;
      }

      String content = item.title() + " " + cleanedSummary;
    Set<Long> matchedInterestIds = keywordInterestCache.findInterestIdsFromContent(content);

    return NewsArticle.builder()
        .sourceName(item.sourceName())
        .title(item.title())
        .link(item.link())
        .summary(cleanedSummary)
        .publishedAt(item.publishedAt())
        .interestIds(matchedInterestIds)
        .build();
  }

  private String cleanHtmlContent(String html) {
    if (html == null || html.isBlank()) {
      return "";
    }

    String temp = html;
    temp = temp.replaceAll("(?is)<script[^>]*>.*?</script>", "");
    temp = temp.replaceAll("(?is)<iframe[^>]*>.*?</iframe>", "");
    temp = temp.replaceAll("(?is)<div[^>]*data-player-id[^>]*>.*?</div>", "");
    temp = temp.replaceAll("<[^>]+>", "");
    return HtmlUtils.htmlUnescape(temp.trim());
  }
}