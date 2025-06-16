package com.example.newsforeveryone.newsarticle.batch.reader;

import com.example.newsforeveryone.newsarticle.batch.dto.ChosunRssItemDto;
import com.example.newsforeveryone.newsarticle.batch.dto.HankyungRssItemDto;
import com.example.newsforeveryone.newsarticle.batch.dto.YonhapRssItemDto;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Slf4j
@Configuration
public class RssReaderConfig {

  @Bean
  @StepScope
  public StaxEventItemReader<ChosunRssItemDto> chosunItemReader(
      @Value("#{jobParameters['chosunUrl']}") String rssUrl) {
    return createStaxReader(rssUrl, ChosunRssItemDto.class);
  }

  @Bean
  @StepScope
  public StaxEventItemReader<HankyungRssItemDto> hankyungItemReader(
      @Value("#{jobParameters['hankyungUrl']}") String rssUrl) {
    return createStaxReader(rssUrl, HankyungRssItemDto.class);
  }

  @Bean
  @StepScope
  public StaxEventItemReader<YonhapRssItemDto> yonhapItemReader(
      @Value("#{jobParameters['yonhapUrl']}") String rssUrl) {
    return createStaxReader(rssUrl, YonhapRssItemDto.class);
  }

  private <T> StaxEventItemReader<T> createStaxReader(String url, Class<T> dtoClass) {
    if (url == null || url.isBlank()) {
      log.warn("URL이 비어있어 해당 스텝을 건너뜁니다");
      return new StaxEventItemReader<>();
    }

    StaxEventItemReader<T> reader = new StaxEventItemReader<>();
    try {
      reader.setResource(new UrlResource(url));
    } catch (MalformedURLException e) {
      log.warn("URL 형식이 잘못되어 해당 스텝을 건너뜁니다. URL: {}", url);
      reader.setResource(new InputStreamResource(
          new ByteArrayInputStream("<items></items>".getBytes(StandardCharsets.UTF_8))));
    }

    reader.setFragmentRootElementName("item");

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setClassesToBeBound(dtoClass);
    reader.setUnmarshaller(marshaller);

    return reader;
  }
}