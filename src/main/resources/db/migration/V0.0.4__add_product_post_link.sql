-- 각 게시글이 어떤 링크를 사용했는지 연관관계 맺는 테이블
CREATE TABLE "product_post_link" (
    "post_id" BIGINT NOT NULL,
    "link_id" BIGINT NOT NULL,
    CONSTRAINT "product_post_link_pkey" PRIMARY KEY ("post_id", "link_id"),
    CONSTRAINT "product_post_link_post_fk" FOREIGN KEY ("post_id") REFERENCES "product_post"("id") ON DELETE CASCADE,
    CONSTRAINT "product_post_link_link_fk" FOREIGN KEY ("link_id") REFERENCES "product_link"("id") ON DELETE CASCADE
);
