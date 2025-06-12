-- 1. 통계 테이블(news_article_metric) 생성
-- 기사(news_article)와 1:1 관계를 가지며, 댓글 수와 조회수를 저장합니다.
CREATE TABLE news_article_metric
(
    article_id    BIGINT PRIMARY KEY,
    comment_count BIGINT NOT NULL DEFAULT 0,
    view_count    BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_news_article_metric_article
        FOREIGN KEY (article_id)
            REFERENCES news_article (id)
            ON DELETE CASCADE
);

-- 2. 기존 데이터 마이그레이션 (Back-filling)
-- 스크립트 실행 시점에 이미 news_article 테이블에 데이터가 존재할 경우,
-- 해당 데이터들의 초기 통계 정보를 계산하여 삽입합니다.
INSERT INTO news_article_metric (article_id, view_count, comment_count)
SELECT a.id,
       (SELECT COUNT(*) FROM article_view av WHERE av.article_id = a.id),
       (SELECT COUNT(*) FROM comment c WHERE c.article_id = a.id AND c.deleted_at IS NULL)
FROM news_article a;


-- 3. 트리거 함수 및 트리거 생성
-- 3-1. 새 기사 생성 / 소프트 삭제 복구 시 통계 테이블 초기화 함수
CREATE OR REPLACE FUNCTION upsert_article_metric() RETURNS TRIGGER AS
$$
BEGIN
INSERT INTO news_article_metric(article_id, comment_count, view_count)
VALUES (NEW.id, 0, 0)
    ON CONFLICT (article_id) DO NOTHING;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER article_metric_upsert_trigger
    AFTER INSERT OR UPDATE
                        ON news_article
                        FOR EACH ROW
                        EXECUTE FUNCTION upsert_article_metric();


-- 3-2. 기사 소프트 삭제 시 통계 테이블 데이터 삭제 함수
CREATE OR REPLACE FUNCTION delete_article_metric_on_soft_delete() RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
DELETE FROM news_article_metric WHERE article_id = NEW.id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER article_metric_soft_delete_trigger
    AFTER UPDATE
    ON news_article
    FOR EACH ROW
    EXECUTE FUNCTION delete_article_metric_on_soft_delete();


-- 3-3. 댓글 수 업데이트 함수 및 트리거
CREATE OR REPLACE FUNCTION update_comment_count_metric() RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'INSERT') THEN
UPDATE news_article_metric SET comment_count = comment_count + 1 WHERE article_id = NEW.article_id;
RETURN NEW;
ELSIF (TG_OP = 'DELETE') THEN
UPDATE news_article_metric SET comment_count = comment_count - 1 WHERE article_id = OLD.article_id;
RETURN OLD;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER comment_count_metric_trigger
    AFTER INSERT OR DELETE
ON comment
    FOR EACH ROW
EXECUTE FUNCTION update_comment_count_metric();


-- 3-4. 조회수 업데이트 함수 및 트리거
CREATE OR REPLACE FUNCTION update_view_count_metric() RETURNS TRIGGER AS
$$
BEGIN
UPDATE news_article_metric SET view_count = view_count + 1 WHERE article_id = NEW.article_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER view_count_metric_trigger
    AFTER INSERT
    ON article_view
    FOR EACH ROW
    EXECUTE FUNCTION update_view_count_metric();

-- article_backup_history 변경
-- 1. 컬럼 삭제
ALTER TABLE article_backup_history DROP COLUMN IF EXISTS record_count;
ALTER TABLE article_backup_history DROP COLUMN IF EXISTS backup_file_url;

-- 2. backup_date DATE → TIMESTAMPTZ (Instant 대응)
ALTER TABLE article_backup_history
ALTER COLUMN backup_date TYPE timestamp with time zone
        USING backup_date::timestamp with time zone;

-- source type column 추가
ALTER TABLE source ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'RSS';