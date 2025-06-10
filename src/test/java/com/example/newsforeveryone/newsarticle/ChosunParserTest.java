package com.example.newsforeveryone.newsarticle;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.parser.ChosunParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ChosunParserTest {

  @InjectMocks
  private ChosunParser chosunParser;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private Resource mockResource;

  @Test
  @DisplayName("정상적인 RSS XML 파싱 및 RssRawArticleDto 매핑 테스트")
  void parse_ValidXml_ReturnsCorrectlyMappedArticles() throws IOException {
    // Given
    String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss xmlns:atom="http://www.w3.org/2005/Atom" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:sy="http://purl.org/rss/1.0/modules/syndication/" version="2.0" xmlns:media="http://search.yahoo.com/mrss/">
                    <channel>
                        <title><![CDATA[조선일보]]></title>
                        <link>https://www.chosun.com</link>
                        <item>
                            <title><![CDATA[50m 상공서 안전장치 풀더니 “내려줘”... 패러세일링 하던 10대 추락사]]></title>
                            <link>https://www.chosun.com/international/international_general/2025/06/04/N4I6UFSUVZCYNPCV6KMLHWII6Q/</link>
                            <dc:creator><![CDATA[이혜진 기자]]></dc:creator>
                            <pubDate>Wed, 04 Jun 2025 00:23:55 +0000</pubDate>
                            <content:encoded><![CDATA[<img src="test_image1.jpg"/><p>몬테네그로 부드바 해변에서 패러세일링을 하던 세르비아 출신 10대 여성이 공중에서 안전장비를 풀고 50m 높이에서 추락해 사망하는 사고가 발생했다. 짧은 요약입니다.</p><p>두 번째 문단 내용.</p>]]></content:encoded>
                        </item>
                        <item>
                            <title><![CDATA[[특징주] 알테오젠, PGR 정식 개시 소식에 9%대 강세]]></title>
                            <link>https://www.chosun.com/economy/money/2025/06/04/CUVA6V62GB6ASCD53EQ44LE56I/</link>
                            <dc:creator><![CDATA[조은서 기자(조선비즈)]]></dc:creator>
                            <pubDate>Wed, 04 Jun 2025 00:22:38 +0000</pubDate>
                            <content:encoded><![CDATA[<img src="test_image2.jpg"/><p>4일 장 초반 알테오젠 주가가 강세다. 알테오젠이 경쟁사인 할로자임의 특허에 대해 제기한 특허무효심판(PGR)을 미국 특허청이 정식 개시한 것으로 알려지면서다. 이 또한 짧은 요약입니다.</p>]]></content:encoded>
                        </item>
                    </channel>
                </rss>
                """;

    InputStream inputStream = new ByteArrayInputStream(sampleXml.getBytes());
    when(restTemplate.getForObject(anyString(), eq(Resource.class))).thenReturn(mockResource);
    when(mockResource.getInputStream()).thenReturn(inputStream);

    // When
    List<RssRawArticleDto> articles = chosunParser.parse("http://test.url/rss", restTemplate);

    // Then
    assertNotNull(articles);
    assertEquals(2, articles.size()); // 두 개의 아이템이 파싱되었는지 확인

    // 첫 번째 기사 검증
    RssRawArticleDto article1 = articles.get(0);
    assertEquals("chosun", article1.sourceName());
    assertEquals("https://www.chosun.com/international/international_general/2025/06/04/N4I6UFSUVZCYNPCV6KMLHWII6Q/", article1.link());
    assertEquals("50m 상공서 안전장치 풀더니 “내려줘”... 패러세일링 하던 10대 추락사", article1.title());
    assertEquals("몬테네그로 부드바 해변에서 패러세일링을 하던 세르비아 출신 10대 여성이 공중에서 안전장비를 풀고 50m 높이에서 추락해 사망하는 사고가 발생했다. 짧은 요약입니다.", article1.summary());
    assertEquals("이혜진 기자", article1.author());
    Instant expectedDate1 = ZonedDateTime.parse("Wed, 04 Jun 2025 00:23:55 +0000", DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    assertEquals(expectedDate1, article1.publishedAt());

    // 두 번째 기사 검증
    RssRawArticleDto article2 = articles.get(1);
    assertEquals("chosun", article2.sourceName());
    assertEquals("https://www.chosun.com/economy/money/2025/06/04/CUVA6V62GB6ASCD53EQ44LE56I/", article2.link());
    assertEquals("[특징주] 알테오젠, PGR 정식 개시 소식에 9%대 강세", article2.title());
    assertEquals("4일 장 초반 알테오젠 주가가 강세다. 알테오젠이 경쟁사인 할로자임의 특허에 대해 제기한 특허무효심판(PGR)을 미국 특허청이 정식 개시한 것으로 알려지면서다. 이 또한 짧은 요약입니다.", article2.summary());
    assertEquals("조은서 기자(조선비즈)", article2.author());
    Instant expectedDate2 = ZonedDateTime.parse("Wed, 04 Jun 2025 00:22:38 +0000", DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    assertEquals(expectedDate2, article2.publishedAt());
  }

  @Test
  @DisplayName("content:encoded에 p 태그가 없는 경우 description이 빈 문자열인지 테스트")
  void parse_NoPtagInContentEncoded_DescriptionIsEmpty() throws IOException {
    // Given
    String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss>
                    <channel>
                        <item>
                            <title><![CDATA[제목]]></title>
                            <link>http://test.com/link</link>
                            <dc:creator><![CDATA[작성자]]></dc:creator>
                            <pubDate>Wed, 04 Jun 2025 00:00:00 +0000</pubDate>
                            <content:encoded><![CDATA[<span>이미지 없음</span><br/>줄바꿈만 있는 내용]]></content:encoded>
                        </item>
                    </channel>
                </rss>
                """;

    InputStream inputStream = new ByteArrayInputStream(sampleXml.getBytes());
    when(restTemplate.getForObject(anyString(), eq(Resource.class))).thenReturn(mockResource);
    when(mockResource.getInputStream()).thenReturn(inputStream);

    // When
    List<RssRawArticleDto> articles = chosunParser.parse("http://test.url/rss", restTemplate);

    // Then
    assertNotNull(articles);
    assertEquals(1, articles.size());
    assertEquals("", articles.get(0).summary()); // p 태그가 없으므로 summary는 빈 문자열
  }
}
