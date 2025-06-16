package com.example.newsforeveryone.newsarticle.batch.client;

import com.example.newsforeveryone.common.exception.ErrorCode;
import com.example.newsforeveryone.common.exception.external.ExternalApiException;
import com.example.newsforeveryone.common.exception.external.RateLimitExceededException;
import com.example.newsforeveryone.newsarticle.batch.client.model.NaverNewsResponse;
import com.example.newsforeveryone.newsarticle.batch.client.model.NaverXmlResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NaverNewsClient {

  @Value("${naver.client.id}")
  private String clientId;

  @Value("${naver.client.secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;

  /**
   * 네이버 뉴스 검색 API 호출
   *
   * @param query   : UTF-8 인코딩된 키워드
   * @param display : 한 번에 가져올 개수 (1~100)
   * @param start   : 시작 위치 (1~1000)
   * @param sort    : sim(정확도) or date(최신순)
   * @return 바인딩된 NaverNewsResponse
   */
  public NaverNewsResponse search(
      String query,
      int display,
      int start,
      String sort
  ) {
    // 인증 header 설정
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Naver-Client-Id", clientId);
    headers.set("X-Naver-Client-Secret", clientSecret);

    // 파라미터 자동 URI 인코딩
    URI uri = UriComponentsBuilder
        .fromUriString("https://openapi.naver.com/v1/search/news.json")
        .queryParam("query", query)
        .queryParam("display", display)
        .queryParam("start", start)
        .queryParam("sort", sort)
        .build()
        .encode(StandardCharsets.UTF_8)
        .toUri();

    // Http 요청 실행
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        NaverNewsResponse.class
    );

    // status code 검사 (200대로 성공이 나오지 않으면 호출 실패)
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IllegalArgumentException("네이버 API호출 실패: " + response.getStatusCode());
    }

    return response.getBody();
  }

  public NaverXmlResponse searchXml(String query, int display, int start, String sort) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Naver-Client-Id", clientId);
    headers.set("X-Naver-Client-Secret", clientSecret);

    URI uri = UriComponentsBuilder
        .fromUriString("https://openapi.naver.com/v1/search/news.xml")
        .queryParam("query", query)
        .queryParam("display", display)
        .queryParam("start", start)
        .queryParam("sort", sort)
        .build()
        .encode(StandardCharsets.UTF_8)
        .toUri();

    HttpEntity<String> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          uri, HttpMethod.GET, entity, String.class);

      JAXBContext jaxbContext = JAXBContext.newInstance(NaverXmlResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (NaverXmlResponse) jaxbUnmarshaller.unmarshal(new StringReader(response.getBody()));

    } catch (HttpClientErrorException e) {

      if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
        Map<String, Object> details = Map.of("uri", uri.toString());
        throw new RateLimitExceededException(details);
      }

      Map<String, Object> details = Map.of("uri", uri.toString(), "message", e.getMessage());
      throw new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR, details);
    } catch (Exception e) {

      Map<String, Object> details = Map.of("uri", uri.toString(), "message", e.getMessage());
      throw new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR, details);
    }
  }
}
