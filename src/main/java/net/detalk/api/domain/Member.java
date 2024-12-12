package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Member {
    private Long id;
    private LoginType loginType;
    private MemberStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @Builder
    public Member(Long id, LoginType loginType, MemberStatus status, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.loginType = loginType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}
