package net.detalk.api.repository;

import net.detalk.api.domain.AuthRefreshToken;

import java.util.Optional;

public interface AuthRefreshTokenRepository {
    AuthRefreshToken save(AuthRefreshToken token);
    Optional<AuthRefreshToken> findByToken(String token);
    AuthRefreshToken update(AuthRefreshToken token);
}
