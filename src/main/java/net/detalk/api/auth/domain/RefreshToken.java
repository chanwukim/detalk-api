package net.detalk.api.auth.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshToken {

    private Long id;
    private Long memberId;
    private String token;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;

    @Builder
    public RefreshToken(Long id, Long memberId, String token, Instant createdAt, Instant expiresAt,
        Instant revokedAt) {
        this.id = id;
        this.memberId = memberId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

}
