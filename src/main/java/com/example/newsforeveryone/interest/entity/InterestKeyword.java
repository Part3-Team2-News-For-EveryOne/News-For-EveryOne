package com.example.newsforeveryone.interest.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interest_keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestKeyword extends BaseEntity {

    @EmbeddedId
    private InterestKeywordId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("interestId")
    private Interest interest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("keywordId")
    private Keyword keyword;

    public InterestKeyword(Interest interest, Keyword keyword) {
        this.interest = interest;
        this.keyword = keyword;
        this.id = new InterestKeywordId(interest.getId(), keyword.getId());
    }
}
