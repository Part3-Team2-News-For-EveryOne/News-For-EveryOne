package com.example.newsforeveryone.newsarticle.entity;

import com.example.newsforeveryone.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Table(name = "users")
@Entity
public class FakeUser extends BaseEntity {

}
