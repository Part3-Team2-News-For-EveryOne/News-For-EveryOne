package com.example.newsforeveryone.newsarticle.batch.parser;

import java.io.InputStream;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * XML Document를 안전하게 생성하는 로직
 * Element에서 텍스트를 추출하는
 * RFC1123 -> Instant
 */
public class RssParserHelper {
  private RssParserHelper(){ // private constructor
  }

  // 외부 엔티티 주입 공격 방지용 설정 메서드
  // 제거해도 될듯
  public static Document buildSecureDocument(InputStream is) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // 보안 설정: DOCTYPE 선언 금지, 외부 엔티티 해제 금지 등
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);

    DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(is);
  }

  // item node element에서 tagname에 해당하는 첫번째 자식 노드의 텍스트 추출
  // 태그가 없거나 공백일 때 null 반환
  public static String extractText(Element parent, String tagName) {
    NodeList nl = parent.getElementsByTagName(tagName);
    if (nl.getLength() == 0) {
      return null;
    }
    String txt = nl.item(0).getTextContent();
    return (txt != null) ? txt.trim() : null;
  }

  // RFC1123 Format의 날짜 문자열을 Intstant로 변환
  // parsing 실패 시 현재 시각을 반환 -> fallback
  public static Instant parseRfc1123Date(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Instant.now();
    }
    try {
      return ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    } catch (Exception ex) {
      // 파싱 실패 시 현재 시각 리턴
      return Instant.now();
    }
  }
}
