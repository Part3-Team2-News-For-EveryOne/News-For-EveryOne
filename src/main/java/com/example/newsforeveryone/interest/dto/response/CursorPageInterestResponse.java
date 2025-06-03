package com.example.newsforeveryone.interest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.engine.transaction.jta.platform.internal.JOnASJtaPlatform;

import java.util.List;

public record CursorPageInterestResponse<T>(
        @JsonProperty("content")
        List<T> contents,

        @JsonProperty("nextCursor")
        String nextCursor,

        @JsonProperty("nextAfter")
        String nextAfter,

        @JsonProperty("size")
        Integer size,

        @JsonProperty("totalElements")
        Integer totalElements,

        @JsonProperty("hasNext")
        boolean hasNext
) {
}
