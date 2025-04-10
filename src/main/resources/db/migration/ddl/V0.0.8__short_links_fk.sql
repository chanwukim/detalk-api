ALTER TABLE "product_post_link"
ADD COLUMN "short_link_id" BIGINT NULL;

COMMENT ON COLUMN "product_post_link"."short_link_id" IS '이 게시글-링크 연결에 해당하는 단축 링크의 ID (short_links.id 참조)';


ALTER TABLE "product_post_link"
ADD CONSTRAINT "product_post_link_short_link_id_fkey" -- 제약 조건 이름 정의
FOREIGN KEY ("short_link_id")                       -- 외래 키로 지정할 컬럼
REFERENCES "short_links"("id")                      -- 참조할 부모 테이블 및 컬럼
ON DELETE SET NULL                                  -- 부모 레코드 삭제 시 동작 설정
ON UPDATE CASCADE;                                  -- 부모 레코드 키 변경 시 동작 설정
