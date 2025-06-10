ALTER TABLE interest
    ALTER COLUMN name
        SET DATA TYPE  varchar(100) COLLATE "ko-KR-x-icu";

ALTER TABLE keyword
    ALTER COLUMN name
        SET DATA TYPE  varchar(100) COLLATE "ko-KR-x-icu";