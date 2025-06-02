-- ================================
-- V1__init_schema.sql
-- Initial DB schema
-- ================================

-- 1. 사용자
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       nickname VARCHAR(100) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at timestamp with time zone NOT NULL,
                       updated_at timestamp with time zone NOT NULL
);

-- 3. 관심사
CREATE TABLE interest (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL UNIQUE,
                          keywords JSONB NOT NULL,
                          created_at timestamp with time zone NOT NULL,
                          updated_at timestamp with time zone NOT NULL
);

-- 4. 구독
CREATE TABLE subscription (
                              subscriber_id BIGINT NOT NULL,
                              interest_id BIGINT NOT NULL,
                              subscribed_at timestamp with time zone NOT NULL,
                              PRIMARY KEY (subscriber_id, interest_id),
                              FOREIGN KEY (subscriber_id) REFERENCES users(id),
                              FOREIGN KEY (interest_id) REFERENCES interest(id)
);

-- 5. 뉴스 기사
CREATE TABLE news_article (
                              id BIGSERIAL PRIMARY KEY,
                              source_name VARCHAR(500) NOT NULL,
                              link VARCHAR(1024) NOT NULL UNIQUE,
                              title VARCHAR(500) NOT NULL,
                              summary TEXT,
                              published_at timestamp with time zone NOT NULL,
                              deleted_at timestamp with time zone DEFAULT NULL,
                              created_at timestamp with time zone NOT NULL,
                              updated_at timestamp with time zone NOT NULL
);

-- 6. 기사 ↔ 관심사
CREATE TABLE article_interest (
                                  article_id BIGINT NOT NULL,
                                  interest_id BIGINT NOT NULL,
                                  PRIMARY KEY (article_id, interest_id),
                                  FOREIGN KEY (article_id) REFERENCES news_article(id),
                                  FOREIGN KEY (interest_id) REFERENCES interest(id)
);

-- 7. 기사 조회 로그
CREATE TABLE article_view (
                              article_id BIGINT NOT NULL,
                              viewer_id BIGINT NOT NULL,
                              viewed_at timestamp with time zone NOT NULL,
                              PRIMARY KEY (article_id, viewer_id),
                              FOREIGN KEY (article_id) REFERENCES news_article(id),
                              FOREIGN KEY (viewer_id) REFERENCES users(id)
);

-- 8. 댓글
CREATE TABLE comment (
                         id BIGSERIAL PRIMARY KEY,
                         article_id BIGINT NOT NULL,
                         user_id BIGINT NOT NULL,
                         content TEXT NOT NULL,
                         deleted_at timestamp with time zone DEFAULT NULL,
                         created_at timestamp with time zone NOT NULL,
                         updated_at timestamp with time zone NOT NULL,
                         FOREIGN KEY (article_id) REFERENCES news_article(id),
                         FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 9. 댓글 좋아요
CREATE TABLE comment_like (
                              comment_id BIGINT NOT NULL,
                              liked_user_id BIGINT NOT NULL,
                              liked_at timestamp with time zone NOT NULL,
                              PRIMARY KEY (comment_id, liked_user_id),
                              FOREIGN KEY (comment_id) REFERENCES comment(id),
                              FOREIGN KEY (liked_user_id) REFERENCES users(id)
);

-- 10. 알림
CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              content TEXT NOT NULL,
                              resource_type VARCHAR(50) NOT NULL,
                              resource_id BIGINT NOT NULL,
                              confirmed BOOLEAN DEFAULT FALSE,
                              created_at timestamp with time zone NOT NULL,
                              updated_at timestamp with time zone NOT NULL,
                              FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 11. 백업 이력
CREATE TABLE article_backup_history (
                                        id BIGSERIAL PRIMARY KEY,
                                        backup_date DATE NOT NULL,
                                        status VARCHAR(20) NOT NULL,
                                        backup_file_url VARCHAR(1024) NOT NULL,
                                        record_count BIGINT,
                                        file_size_bytes BIGINT,
                                        created_at timestamp with time zone NOT NULL,
                                        updated_at timestamp with time zone NOT NULL
);

-- 12. 기사 출처
CREATE TABLE source (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE,
                        feed_url VARCHAR(512) NOT NULL UNIQUE,
                        created_at timestamp with time zone NOT NULL,
                        updated_at timestamp with time zone NOT NULL
);