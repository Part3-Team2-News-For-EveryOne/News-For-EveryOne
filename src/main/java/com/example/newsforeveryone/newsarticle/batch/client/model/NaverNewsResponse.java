package com.example.newsforeveryone.newsarticle.batch.client.model;

import com.example.newsforeveryone.interest.entity.InterestKeyword;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverNewsResponse {
    private InterestKeyword interestKeyword;
    private int total;
    private int start;
    private int display;
    private List<NaverNewsItem> items;
}
