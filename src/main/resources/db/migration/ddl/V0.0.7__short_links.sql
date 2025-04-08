-- 단축 URL 매핑 정보 테이블
CREATE TABLE "short_links"
(
    "id"           BIGINT GENERATED ALWAYS AS IDENTITY, -- 기본 키 (자동 증가)
    "short_code"   VARCHAR(12) NOT NULL,                -- 고유 단축 코드 (12자로 설정, 필요시 조정)
    "original_url" TEXT        NOT NULL CHECK ("original_url" ~ '^https?://.+'
) , -- 원본 URL (http/https 형식 검증);
    "creator_id" BIGINT NULL,                                         -- 생성자 ID (Nullable)
    "created_at" BIGINT NOT NULL,                                     -- 생성 시간 (Epoch milliseconds)

    CONSTRAINT "short_links_pkey" PRIMARY KEY ("id"),                 -- 기본 키 제약 조건;
    CONSTRAINT "short_links_short_code_key" UNIQUE ("short_code"),    -- 단축 코드 고유 제약 조건
    CONSTRAINT "short_links_creator_id_fkey" FOREIGN KEY ("creator_id")
        REFERENCES "member"("id") ON DELETE SET NULL ON UPDATE CASCADE -- 외래 키 (member 테이블 참조);
);

-- COMMENT 추가 (컬럼 설명)
COMMENT ON COLUMN "short_links"."id" IS '단축 링크 고유 ID (자동 생성)';
COMMENT ON COLUMN "short_links"."short_code" IS '고유 단축 코드 (URL 경로에 사용)';
COMMENT ON COLUMN "short_links"."original_url" IS '리디렉션될 원본 URL';
COMMENT ON COLUMN "short_links"."creator_id" IS '링크를 생성한 회원 ID (nullable)';
COMMENT ON COLUMN "short_links"."created_at" IS '링크 생성 시각 (Epoch milliseconds)';

