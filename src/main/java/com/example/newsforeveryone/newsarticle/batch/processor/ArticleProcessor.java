package com.example.newsforeveryone.newsarticle.batch.processor;

import com.example.newsforeveryone.newsarticle.batch.dto.ArticleItemNormalizer;
import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<ArticleItemNormalizer, NewsArticle> {

  private final KeywordInterestCache keywordInterestCache;

  @Override
  public NewsArticle process(ArticleItemNormalizer item) {
    String cleanedTitle = cleanHtmlContent(item.getNormalizedTitle());
    String cleanedSummary = cleanHtmlContent(item.getNormalizedSummary());

    if (cleanedSummary.isBlank() || cleanedTitle.isBlank()) {
      return null;
    }

    String content = cleanedTitle + " " + cleanedSummary;
    Set<Long> matchedInterestIds = keywordInterestCache.findInterestIdsFromContent(content);

    return NewsArticle.builder()
        .sourceName(item.getNormalizedSourceName())
        .title(cleanedTitle)
        .link(item.getNormalizedLink())
        .summary(cleanedSummary)
        .publishedAt(item.getNormalizedPublishedAt())
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