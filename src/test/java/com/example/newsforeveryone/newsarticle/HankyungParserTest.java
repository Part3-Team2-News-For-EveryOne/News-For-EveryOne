package com.example.newsforeveryone.newsarticle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.newsforeveryone.newsarticle.batch.parser.HankyungParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;

@ExtendWith(MockitoExtension.class)
class HankyungParserTest {

    private HankyungParser parser;

    @Mock
    private RestTemplate restTemplate;

    private static final String FEED_URL = "https://www.hankyung.com/feed/all-news";

    @BeforeEach
    void setUp() {
        parser = new HankyungParser(restTemplate);
    }

    @Test
    void testSupports_trueForHankyungUrl() {
        // "hankyung.com"이 포함된 URL에 대해 true 반환
        assertTrue(parser.supports(FEED_URL),
            "hankyung 도메인이 포함된 URL은 supports()가 true를 반환해야 합니다.");
    }

    @Test
    void testSupports_falseForOtherUrl() {
        // 한경 도메인이 아닌 URL에 대해 false 반환
        assertFalse(parser.supports("https://www.yonhapnewstv.co.kr/browse/feed/"),
            "hankyung 도메인이 포함되지 않은 URL은 supports()가 false를 반환해야 합니다.");
    }

    @Test
    void testMapItem_success() throws Exception {
        // <item> XML 조각 생성 (description 없이 title이 summary가 됨)
        String xml = ""
            + "<item>"
            +   "<title>한경 테스트 제목</title>"
            +   "<link>http://test.hankyung.link</link>"
            +   "<author>기자홍길동</author>"
            +   "<pubDate>Mon, 09 Jun 2025 09:15:00 +0900</pubDate>"
            + "</item>";

        Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Element item = (Element) doc.getElementsByTagName("item").item(0);

        RssRawArticleDto dto = parser.mapItem(item);

        assertAll("mapped dto",
            () -> assertEquals("한경RSS", dto.sourceName()),
            () -> assertEquals("http://test.hankyung.link", dto.link()),
            () -> assertEquals("한경 테스트 제목", dto.title()),
            // summary는 title과 동일해야 함
            () -> assertEquals("한경 테스트 제목", dto.summary()),
            () -> assertEquals("기자홍길동", dto.author()),
            () -> assertNotNull(dto.publishedAt())
        );
    }

    @Test
    void testParse_success() throws Exception {
        // 전체 RSS 피드 XML 예시: <rss><channel><item>...</item>...</channel></rss>
        String feedXml = ""
            + "<rss><channel>"
            +   "<item>"
            +     "<title>제목1</title>"
            +     "<link>http://link1</link>"
            +     "<author>저자1</author>"
            +     "<pubDate>Mon, 09 Jun 2025 08:00:00 +0900</pubDate>"
            +   "</item>"
            +   "<item>"
            +     "<title>제목2</title>"
            +     "<link>http://link2</link>"
            +     "<author>저자2</author>"
            +     "<pubDate>Mon, 09 Jun 2025 08:30:00 +0900</pubDate>"
            +   "</item>"
            + "</channel></rss>";

        Resource resource = (Resource) new ByteArrayResource(feedXml.getBytes(StandardCharsets.UTF_8));
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(resource);

        List<RssRawArticleDto> result = parser.parse(FEED_URL, restTemplate);

        assertEquals(2, result.size(), "두 개의 item이 파싱되어야 합니다.");
        assertEquals("제목1", result.get(0).title());
        assertEquals("http://link2", result.get(1).link());
    }

    @Test
    void testParse_nullResource_returnsEmptyList() {
        // RestTemplate이 null 반환 시 빈 리스트 검증
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(null);

        List<RssRawArticleDto> result = parser.parse(FEED_URL, restTemplate);
        assertTrue(result.isEmpty(),
            "Resource가 null인 경우 parse()는 빈 리스트를 반환해야 합니다.");
    }

    @Test
    void testParse_malformedXml_returnsEmptyList() {
        // 잘못된 XML로 파싱 오류 유도
        String badXml = "<rss><channel><item><title>열리지않음";
        Resource resource = (Resource) new ByteArrayResource(badXml.getBytes(StandardCharsets.UTF_8));
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(resource);

        List<RssRawArticleDto> result = parser.parse(FEED_URL, restTemplate);
        assertTrue(result.isEmpty(),
            "XML 파싱 오류 시 parse()는 빈 리스트를 반환해야 합니다.");
    }
}
