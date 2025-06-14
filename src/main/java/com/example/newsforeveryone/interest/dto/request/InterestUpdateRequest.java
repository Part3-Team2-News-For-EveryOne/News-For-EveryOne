package com.example.newsforeveryone.interest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InterestUpdateRequest(
    @JsonProperty("keywords")
    List<String> keywords
) {

}
