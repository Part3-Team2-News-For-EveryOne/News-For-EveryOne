package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;
import com.example.newsforeveryone.interest.entity.Keyword;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Slice;

public interface InterestKeywordCustom {

  Slice<Interest> searchInterestByWordUsingCursor(String keyword, String orderBy, String direction,
      String cursor, String after, Integer limit);

  Map<Interest, List<Keyword>> groupKeywordsByUserInterests(List<Interest> InterestsWithWord);

  Long countByInterestWord(String word, List<Interest> keywordInterestIds);

}
