package com.example.newsforeveryone.newsarticle.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageArticleRequest(
    String keyword,
    Long interestId,
    List<String> sourceIn,
    Instant publishDateFrom,
    Instant publishDateTo,
    String orderBy,
    String direction,
    String cursor,
    Instant after,
    Integer limit
//    Long monewRequestUserID 이건 controller 에서 따로 받기

) {

}
