package com.example.newsforeveryone.newsarticle;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.parser.ChosunParser;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChosunParserTest {

  @InjectMocks
  private ChosunParser chosunParser;

  @Mock
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    chosunParser = new ChosunParser(restTemplate);
  }

  @Test
  void testMapItem_success() throws Exception {
    // Given: 조선일보 RSS의 <item> 단일 XML 조각 (CDATA 포함)
    String xml = ""
        + "<item>"
        +   "<title><![CDATA[50m 상공서 안전장치 풀더니 “내려줘”... 패러세일링 하던 10대 추락사]]></title>"
        +   "<link>https://www.chosun.com/international/international_general/2025/06/04/N4I6UFSUVZCYNPCV6KMLHWII6Q/</link>"
        +   "<dc:creator><![CDATA[이혜진 기자]]></dc:creator>"
        +   "<pubDate>Wed, 04 Jun 2025 00:23:55 +0000</pubDate>"
        +   "<content:encoded><![CDATA["
        +     "<img src=\"test_image1.jpg\"/>"
        +     "<p>몬테네그로 부드바 해변에서 패러세일링을 하던 세르비아 출신 10대 여성이 공중에서 안전장비를 풀고 50m 높이에서 추락해 사망하는 사고가 발생했다. 짧은 요약입니다.</p>"
        +     "<p>두 번째 문단 내용.</p>"
        +   "]]></content:encoded>"
        + "</item>";

    // XML → DOM 변환
    Document doc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

    Element item = (Element) doc.getElementsByTagName("item").item(0);

    // 단일 <item>을 파싱
    RssRawArticleDto dto = chosunParser.mapItem(item);

    // 각 필드 검증
    assertAll("조선일보 <item> 파싱 검증",
        () -> assertEquals("조선RSS", dto.sourceName()),
        () -> assertEquals("https://www.chosun.com/international/international_general/2025/06/04/N4I6UFSUVZCYNPCV6KMLHWII6Q/", dto.link()),
        () -> assertEquals("50m 상공서 안전장치 풀더니 “내려줘”... 패러세일링 하던 10대 추락사", dto.title()),
        () -> assertEquals("몬테네그로 부드바 해변에서 패러세일링을 하던 세르비아 출신 10대 여성이 공중에서 안전장비를 풀고 50m 높이에서 추락해 사망하는 사고가 발생했다. 짧은 요약입니다.", dto.summary()),
        () -> assertEquals("이혜진 기자", dto.author()),
        () -> assertNotNull(dto.publishedAt(), "발행일시가 null이 아니어야 함")
    );
  }

  @Test
  @DisplayName("content:encoded에 p 태그가 없는 경우 summary는 빈 문자열이어야 한다")
  void testMapItem_NoPtagInContentEncoded_DescriptionIsEmpty() throws Exception {
    String xml = ""
        + "<item xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">"
        +   "<title><![CDATA[제목]]></title>"
        +   "<link>http://test.com/link</link>"
        +   "<dc:creator><![CDATA[작성자]]></dc:creator>"
        +   "<pubDate>Wed, 04 Jun 2025 00:00:00 +0000</pubDate>"
        +   "<content:encoded><![CDATA[<span>이미지 없음</span><br/>줄바꿈만 있는 내용]]></content:encoded>"
        + "</item>";


    // DOM 파싱
    Document doc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

    Element item = (Element) doc.getElementsByTagName("item").item(0);

    // 개별 item 파싱
    RssRawArticleDto dto = chosunParser.mapItem(item);

    // 요약 필드가 빈 문자열인지 검증
    assertAll("p 태그가 없을 경우 요약은 빈 문자열",
        () -> assertEquals("제목", dto.title()),
        () -> assertEquals("http://test.com/link", dto.link()),
        () -> assertEquals("작성자", dto.author()),
        () -> assertNotNull(dto.publishedAt()),
        () -> assertEquals("", dto.summary(), "<p> 태그가 없으므로 summary는 빈 문자열이어야 함")
    );
  }

}
