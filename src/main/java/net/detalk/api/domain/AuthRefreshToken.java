package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;
import net.detalk.api.support.TimeHolder;

import java.time.Instant;

@Getter
public class AuthRefreshToken {
    private Long id;
    private Long memberId;
    private String token;
    private Instant createdAt;
    /**
     * 만료 예정 시간
     */
    private Instant expiresAt;
    /**
     * 실제 만료된 시간
     */
    private Instant revokedAt;

    @Builder
    public AuthRefreshToken(Long id, Long memberId, String token, Instant createdAt, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.memberId = memberId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public void revoked(TimeHolder timeHolder) {
        this.revokedAt = timeHolder.now();
    }
}
