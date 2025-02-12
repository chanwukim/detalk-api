-- idempotent_key: 클라이언트가 생성한 UUID를 저장. UNIQUE 제약을 통해 중복 삽입 시 에러가 발생하게 함
CREATE TABLE product_post_idempotent_requests (
    "id" BIGINT GENERATED ALWAYS AS IDENTITY,
    "idempotent_key" UUID NOT NULL,
    "created_at" BIGINT NOT NULL,
    CONSTRAINT "ux_idempotent_key" UNIQUE ("idempotent_key")

);
