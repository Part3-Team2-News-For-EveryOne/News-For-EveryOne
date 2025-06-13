package com.example.newsforeveryone.interest.repository;

import com.example.newsforeveryone.interest.entity.Interest;
import java.util.List;
import java.util.Map;

public interface InterestKeywordCustom {

  List<Interest> searchInterestByWordUsingCursor(String keyword, String orderBy, String direction,
      String cursor, String after, Integer limit);

  Map<Interest, List<String>> groupKeywordsByInterest(List<Interest> InterestsWithWord);

  Long countSearchInterest(String word, List<Interest> keywordInterestIds);

}
