package com.example.newsforeveryone.newsarticle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.newsforeveryone.newsarticle.batch.dto.RssRawArticleDto;
import com.example.newsforeveryone.newsarticle.batch.parser.YonhapParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ExtendWith(MockitoExtension.class)
class YonhapParserTest {

    private YonhapParser yonhapParser;

    @Mock
    private RestTemplate restTemplate;
    private static final String FEED_URL = "https://www.yonhapnewstv.co.kr/browse/feed/";

    // 실제 HTTP 호출 없는 test진행으로 restTemplate 인스턴스로 충분
    @BeforeEach
    void setUp() {
        this.yonhapParser = new YonhapParser(restTemplate);
    }

    // feedUrl이 맞는지 검증
    @Test
    void testSupports_true(){ assertTrue(yonhapParser.supports(FEED_URL), "yonhap URL is return true"); }
    @Test
    void testSupports_false(){
        // 다른 url
        String url = "https://www.hankyung.com/feed/all-news";
        assertFalse(yonhapParser.supports(url), "hankyung URL is return false");
    }

    // xml 이 제대로 parsing되고 있는지 검증
    @Test
    void testMapItem_success() throws Exception {
        String xml = "<item>"
            + "<title>테스트 제목</title>"
            + "<link>http://yonhapnewstv.co.kr/test-link</link>"
            + "<dc:creator>기자이름</dc:creator>"
            + "<pubDate>Sun, 08 Jun 2025 14:30:00 +0900</pubDate>"
            + "<description>요약 텍스트</description>"
            + "</item>";
        Document document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Element item = (Element) document.getElementsByTagName("item").item(0);
        RssRawArticleDto dto = yonhapParser.mapItem(item);

        assertAll("mapped dto",
            () -> assertEquals("연합RSS", dto.sourceName()),
            () -> assertEquals("http://yonhapnewstv.co.kr/test-link", dto.link()),
            () -> assertEquals("테스트 제목", dto.title()),
            () -> assertEquals("요약 텍스트", dto.summary()),
            () -> assertEquals("기자이름", dto.author()),
            () -> assertNotNull(dto.publishedAt())
        );
    }

    //
    @Test
    void testMapItem_fail() throws Exception {
        String xml = "<rss><channel>"
            + "<item>"
            +  "<title>false title</title>"
            +   "<link>http://link1</link>"
            +   "<description>요약1</description>"
            +   "<dc:creator>저자1</dc:creator>"
            +   "<pubDate>Sun, 08 Jun 2025 10:00:00 +0900</pubDate>"
            + "</item>"
            + "<item>"
            +   "<title>제목2</title>"
            +   "<link>http://link2</link>"
            +   "<description>요약2</description>"
            +   "<dc:creator>저자2</dc:creator>"
            +   "<pubDate>Sun, 08 Jun 2025 11:00:00 +0900</pubDate>"
            + "</item>"
            + "</channel></rss>";

        // xml byte를 감싼 스프링 resource 구현체
        Resource resource = new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8));
        // 방금 만든 resource를 반환하도록 stub한다.
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(resource);

        List<RssRawArticleDto> list = yonhapParser.parse(FEED_URL, restTemplate);

        // item요소를 두개로 했으므로 list 크기도 2
        assertAll(
            () -> assertEquals(2, list.size(), "아이템 2개가 파싱되어야 합니다."),
            () -> assertEquals("false title", list.get(0).title()),
            () -> assertEquals("http://link2", list.get(1).link())
        );
    }

    // 외부 호출 실패 등으로 restTemplate.getForObject()가 null을 반환하는 상황 가정
    // parse() 가 예외없이 빈 리스트를 반환하는지 검증
    @Test
    void testParse_nullResource_returnsEmptyList() {
        // RestTemplate이 null 반환하도록 설정
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(null);
        // 실제 동작 확인
        List<RssRawArticleDto> list = yonhapParser.parse(FEED_URL, restTemplate);
        // 동작이 예상대로 나왔는지 확인
        assertTrue(list.isEmpty(), "Resource가 null인 경우 빈 리스트를 반환해야 합니다.");
    }

    // rss xml이 구문 오류로 인해 parsing중 예외가 발생하는 경우
    // parse가 예외를 밖으로 던지지 않고, 내부에서 예외를 잡고 빈 리스트 반환 검증
    @Test
    void testParse_malformedXml_returnsEmptyList() {
        // XML 파싱 오류 발생하도록 잘못된 XML 제공
        String badXml = "<rss><channel><item><title>no close tags";
        Resource resource = new ByteArrayResource(badXml.getBytes(StandardCharsets.UTF_8));
        given(restTemplate.getForObject(eq(FEED_URL), eq(Resource.class)))
            .willReturn(resource);

        // 내부에서 예외를 잡고 빈 리스트를 반환하도록 설계되어 있음
        List<RssRawArticleDto> list = yonhapParser.parse(FEED_URL, restTemplate);

        assertTrue(list.isEmpty(), "파싱 오류 발생 시 빈 리스트를 반환해야 합니다.");
    }
}