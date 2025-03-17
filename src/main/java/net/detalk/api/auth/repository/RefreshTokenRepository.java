package net.detalk.api.auth.repository;

import java.time.Instant;
import net.detalk.api.auth.domain.RefreshToken;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);

    void revokeByMemberId(Long memberId, Instant now);

    boolean isActiveToken(String token, boolean revoked);
}
