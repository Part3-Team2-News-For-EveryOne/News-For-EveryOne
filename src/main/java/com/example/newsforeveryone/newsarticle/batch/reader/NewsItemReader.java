package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.newsarticle.batch.dto.RawArticleDto;
import org.springframework.batch.item.ItemReader;

public interface NewsItemReader extends ItemReader<RawArticleDto> {

}
