-- 단축 URL 매핑 정보 테이블
CREATE TABLE "short_links" (
     "id" BIGINT GENERATED ALWAYS AS IDENTITY,
    "short_code" VARCHAR(12) NOT NULL,
     "original_url" TEXT NOT NULL CHECK ("original_url" ~ '^https?://.+'),
    "creator_id" BIGINT NULL,
    "created_at" BIGINT NOT NULL,

    CONSTRAINT "short_links_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "short_links_short_code_key" UNIQUE ("short_code"),
    CONSTRAINT "short_links_creator_id_fkey" FOREIGN KEY ("creator_id")
        REFERENCES "member"("id") ON DELETE SET NULL ON UPDATE CASCADE
);

-- 컬럼 설명
COMMENT ON COLUMN "short_links"."id" IS '단축 링크 고유 ID (자동 생성)';
COMMENT ON COLUMN "short_links"."short_code" IS '고유 단축 코드 (URL 경로에 사용)';
COMMENT ON COLUMN "short_links"."original_url" IS '리디렉션될 원본 URL';
COMMENT ON COLUMN "short_links"."creator_id" IS '링크를 생성한 회원 ID (nullable)';
COMMENT ON COLUMN "short_links"."created_at" IS '링크 생성 시각 (Epoch milliseconds)';


-- 클릭 로그 테이블
CREATE TABLE "short_links_logs"
(
    "id"          BIGINT GENERATED ALWAYS AS IDENTITY,
    "link_id"     BIGINT NOT NULL,                                     -- 참조하는 short_links 테이블의 id
    "clicked_at"  BIGINT NOT NULL,                                     -- 클릭 발생 시각 (Epoch milliseconds)
    "ip_address"  INET NULL,                                           -- 클릭한 사용자의 IP 주소
    "user_agent"  TEXT NULL,                                           -- 클릭한 사용자의 User-Agent 문자열
    "referrer"    TEXT NULL,                                           -- 클릭 유입 경로 URL
    "country"     VARCHAR(100) NULL,                                   -- IP 기반 국가 정보
    "city"        VARCHAR(100) NULL,                                   -- IP 기반 도시 정보
    "device_type" VARCHAR(50) NULL,                                    -- User-Agent 기반 기기 종류
    "os"          VARCHAR(50) NULL,                                    -- User-Agent 기반 운영 체제
    "browser"     VARCHAR(50) NULL,                                    -- User-Agent 기반 브라우저;

    CONSTRAINT "short_links_logs_pkey" PRIMARY KEY ("id"),             -- 제약조건
    CONSTRAINT "short_links_logs_link_id_fkey" FOREIGN KEY ("link_id") -- 제약조건
        REFERENCES "short_links" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);


-- 컬럼 설명
COMMENT ON TABLE "short_links_logs" IS '단축 링크 클릭 이벤트 로그';
COMMENT ON COLUMN "short_links_logs"."id" IS '로그 고유 ID (자동 생성)';
COMMENT ON COLUMN "short_links_logs"."link_id" IS '클릭된 단축 링크의 ID (short_links.id 참조)';
COMMENT ON COLUMN "short_links_logs"."clicked_at" IS '클릭 발생 시각 (Epoch milliseconds)';
COMMENT ON COLUMN "short_links_logs"."ip_address" IS '클릭한 사용자의 IP 주소';
COMMENT ON COLUMN "short_links_logs"."user_agent" IS '클릭한 사용자의 User-Agent 정보';
COMMENT ON COLUMN "short_links_logs"."referrer" IS '클릭 유입 경로 (이전 페이지 URL)';
COMMENT ON COLUMN "short_links_logs"."country" IS 'IP 주소 기반 국가 정보';
COMMENT ON COLUMN "short_links_logs"."city" IS 'IP 주소 기반 도시 정보';
COMMENT ON COLUMN "short_links_logs"."device_type" IS 'User-Agent 기반 기기 종류 (PC, Mobile, Tablet 등)';
COMMENT ON COLUMN "short_links_logs"."os" IS 'User-Agent 기반 운영 체제 정보';
COMMENT ON COLUMN "short_links_logs"."browser" IS 'User-Agent 기반 브라우저 정보';