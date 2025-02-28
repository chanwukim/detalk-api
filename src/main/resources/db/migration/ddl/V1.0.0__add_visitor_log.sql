-- 방문자 로그 테이블 생성: 사용자 위치 및 방문 정보를 저장
CREATE TABLE "visitor_log" (
    "id" BIGSERIAL PRIMARY KEY,
    "session_id" VARCHAR(255) NOT NULL, -- 방문자의 세션 ID
    "continent_code" CHAR(2) NOT NULL, -- 방문자의 대륙 코드 (예: 'NA', 'AS')
    "country_iso" CHAR(2) NOT NULL, -- 방문자의 국가 ISO 코드 (예: 'US', 'KR')
    "country_name" VARCHAR(64) NOT NULL, -- 방문자의 국가명 (예: 'United States', 'South Korea')
    "visited_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 방문 시각
    "user_agent" VARCHAR(512) NULL, -- 방문자의 브라우저 정보
    "referer" VARCHAR(512) NULL -- 이전 페이지 정보
);