package net.detalk.api.auth.repository;

import net.detalk.api.auth.domain.AuthRefreshToken;

import java.util.Optional;

public interface AuthRefreshTokenRepository {
    AuthRefreshToken save(AuthRefreshToken token);
    Optional<AuthRefreshToken> findByToken(String token);
    AuthRefreshToken update(AuthRefreshToken token);
}
