package net.detalk.api.repository;

import net.detalk.api.domain.AuthRefreshToken;

public interface AuthRefreshTokenRepository {
    AuthRefreshToken save(AuthRefreshToken token);
}
