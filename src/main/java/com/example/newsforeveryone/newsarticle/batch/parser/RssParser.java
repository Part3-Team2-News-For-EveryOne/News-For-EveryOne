package com.example.newsforeveryone.newsarticle.batch.parser;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import java.util.List;

public interface RssParser {
  List<RssRawArticleDto> parse(String url);
}
