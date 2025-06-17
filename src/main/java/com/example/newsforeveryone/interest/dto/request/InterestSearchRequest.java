package com.example.newsforeveryone.interest.dto.request;

public record InterestSearchRequest(
    String searchWord,
    String orderBy,
    String direction,
    String cursor,
    String after,
    Integer limit
) {

  public static InterestSearchRequest of(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      String after,
      Integer limit
  ) {
    return new InterestSearchRequest(
        adjustWord(keyword),
        orderBy,
        direction,
        cursor,
        after,
        adjustLimit(limit)
    );
  }

  private static int adjustLimit(Integer limit) {
    if (limit == null || limit < 50) {
      return 50;
    }
    return limit;
  }

  private static String adjustWord(String word) {
    if (word == null) {
      return "";
    }
    return word;
  }

}
