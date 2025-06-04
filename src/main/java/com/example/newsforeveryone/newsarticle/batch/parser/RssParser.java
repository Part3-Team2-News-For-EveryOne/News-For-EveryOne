package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.util.List;

public interface RssParser {
  boolean supports(String feedUrl);
  List<RssRawArticleDto> parse(String url);
}
