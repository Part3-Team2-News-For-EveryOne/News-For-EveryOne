package com.example.newsforeveryone.interest.entity.id;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestKeywordId implements Serializable {

    private Long interestId;
    private Long keywordId;

    public InterestKeywordId(Long interestId, Long keywordId) {
        this.interestId = interestId;
        this.keywordId = keywordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterestKeywordId that = (InterestKeywordId) o;
        return interestId.equals(that.interestId) && keywordId.equals(that.keywordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interestId, keywordId);
    }

}

