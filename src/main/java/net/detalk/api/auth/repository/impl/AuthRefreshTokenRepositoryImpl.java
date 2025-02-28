package net.detalk.api.auth.repository.impl;

import lombok.RequiredArgsConstructor;
import net.detalk.api.auth.repository.AuthRefreshTokenRepository;
import net.detalk.api.auth.domain.AuthRefreshToken;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static net.detalk.jooq.Tables.AUTH_REFRESH_TOKEN;

@Repository
@RequiredArgsConstructor
public class AuthRefreshTokenRepositoryImpl implements AuthRefreshTokenRepository {
    private final DSLContext dsl;

    @Override
    public AuthRefreshToken save(AuthRefreshToken token) {
        return dsl.insertInto(AUTH_REFRESH_TOKEN)
            .set(AUTH_REFRESH_TOKEN.MEMBER_ID, token.getMemberId())
            .set(AUTH_REFRESH_TOKEN.TOKEN, token.getToken())
            .set(AUTH_REFRESH_TOKEN.CREATED_AT, token.getCreatedAt())
            .set(AUTH_REFRESH_TOKEN.EXPIRES_AT, token.getExpiresAt())
            .set(AUTH_REFRESH_TOKEN.REVOKED_AT, token.getRevokedAt())
            .returning()
            .fetchOneInto(AuthRefreshToken.class);
    }

    @Override
    public Optional<AuthRefreshToken> findByToken(String token) {
        return dsl.selectFrom(AUTH_REFRESH_TOKEN)
            .where(AUTH_REFRESH_TOKEN.TOKEN.eq(token))
            .fetchOptionalInto(AuthRefreshToken.class);
    }

    @Override
    public AuthRefreshToken update(AuthRefreshToken token) {
        return dsl.update(AUTH_REFRESH_TOKEN)
            .set(AUTH_REFRESH_TOKEN.REVOKED_AT, token.getRevokedAt())
            .where(AUTH_REFRESH_TOKEN.ID.eq(token.getId()))
            .returning()
            .fetchOneInto(AuthRefreshToken.class);
    }
}
