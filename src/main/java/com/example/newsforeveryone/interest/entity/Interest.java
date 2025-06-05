package com.example.newsforeveryone.interest.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interest_seq_gen")
    @SequenceGenerator(name = "interest_seq_gen", sequenceName = "interest_id_seq", allocationSize = 50)
    private Long id;

    @JoinColumn(name = "interestName", nullable = false, unique = true)
    private String name;

    public Interest(String name) {
        this.name = name;
    }

}
