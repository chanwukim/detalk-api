package net.detalk.api.repository;

import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.AuthRefreshToken;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static net.detalk.jooq.Tables.AUTH_REFRESH_TOKEN;

@Repository
@RequiredArgsConstructor
public class AuthRefreshTokenRepositoryImpl implements AuthRefreshTokenRepository{
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
}
