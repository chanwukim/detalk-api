CREATE TABLE "role" (
    "code" VARCHAR(32) PRIMARY KEY,
    "description" VARCHAR(255)
);

CREATE TABLE "member_role" (
    "member_id" BIGINT NOT NULL,
    "role_code" VARCHAR(32) NOT NULL,
    PRIMARY KEY ("member_id", "role_code"),
    FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("role_code") REFERENCES "role" ("code") ON DELETE CASCADE
);