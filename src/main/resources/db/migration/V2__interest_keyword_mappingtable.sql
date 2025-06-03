-- 1. interest 테이블 키워드 칼럼 삭제
ALTER TABLE interest DROP COLUMN keywords;

-- 2. 키워드 테이들 생성
CREATE TABLE keyword
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL UNIQUE,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);


-- 3. 관심사 ↔ 키워드 (N:M) 매핑
CREATE TABLE interest_keyword
(
    interest_id BIGINT                   NOT NULL,
    keyword_id  BIGINT                   NOT NULL,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone NOT NULL,
    PRIMARY KEY (interest_id, keyword_id)
);

-- 4.  subscription 테이블 created_at, updated_at 추가
ALTER TABLE subscription
    ADD COLUMN created_at timestamp with time zone NOT NULL,
    ADD COLUMN updated_at timestamp with time zone NOT NULL;

