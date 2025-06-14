package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import org.springframework.data.domain.Slice;

public interface InterestKeywordCustom {

  Slice<Interest> searchInterestByWordWithCursor(String keyword, String orderBy, String direction,
      String cursor, String after, Integer limit);

}
