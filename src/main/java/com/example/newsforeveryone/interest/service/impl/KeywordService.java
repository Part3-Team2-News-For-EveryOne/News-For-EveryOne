package com.example.newsforeveryone.interest.service.impl;

import com.example.newsforeveryone.interest.entity.Keyword;
import com.example.newsforeveryone.interest.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public List<Keyword> registerKeyword(List<String> keywords, double threshold) {
        if (keywords == null) {
            throw new IllegalArgumentException("키워드 값이 존재하지 않습니다.");
        }

        List<Keyword> existingWords = new ArrayList<>();
        List<Keyword> newKeywords = new ArrayList<>();
        for (String keyword : keywords) {
            keywordRepository.findMaxSimilarityKeyword(keyword, threshold)
                    .ifPresentOrElse(existingWords::add, () -> newKeywords.add(new Keyword(keyword)));
        }

        List<Keyword> savedNewKeywords = keywordRepository.saveAll(newKeywords);
        existingWords.addAll(savedNewKeywords);

        return existingWords;
    }

}
