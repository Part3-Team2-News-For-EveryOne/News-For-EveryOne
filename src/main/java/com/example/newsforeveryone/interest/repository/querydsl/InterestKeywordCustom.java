package com.example.newsforeveryone.interest.repository.querydsl;

import com.example.newsforeveryone.interest.entity.Interest;

import java.util.List;
import java.util.Map;

public interface InterestKeywordCustom {

    Map<Interest, List<String>> searchByWord(String keyword, String orderBy, String direction, String cursor, String after, Integer limit);
}
