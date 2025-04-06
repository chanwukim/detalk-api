package net.detalk.api.auth.repository.impl;

import static net.detalk.jooq.Tables.AUTH_REFRESH_TOKEN;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.detalk.api.auth.domain.RefreshToken;
import net.detalk.api.auth.repository.RefreshTokenRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final DSLContext dsl;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return dsl.insertInto(AUTH_REFRESH_TOKEN)
            .set(AUTH_REFRESH_TOKEN.MEMBER_ID, refreshToken.getMemberId())
            .set(AUTH_REFRESH_TOKEN.TOKEN, refreshToken.getToken())
            .set(AUTH_REFRESH_TOKEN.CREATED_AT, refreshToken.getCreatedAt())
            .set(AUTH_REFRESH_TOKEN.EXPIRES_AT, refreshToken.getExpiresAt())
            .set(AUTH_REFRESH_TOKEN.REVOKED_AT, refreshToken.getRevokedAt())
            .returning()
            .fetchOneInto(RefreshToken.class);
    }

    /**
     * 사용자 ID로 토큰 폐기
     */
    @Override
    public void revokeByMemberId(Long memberId, Instant now) {
        dsl.update(AUTH_REFRESH_TOKEN)
            .set(AUTH_REFRESH_TOKEN.REVOKED_AT, now)
            .where(AUTH_REFRESH_TOKEN.MEMBER_ID.eq(memberId))
            .and(AUTH_REFRESH_TOKEN.REVOKED_AT.isNull())
            .execute();
    }

    @Override
    public boolean isActiveToken(String token, boolean revoked) {
        Condition revokedCondition;

        // 비활성화된 토큰이라면 REVOKED_AT 는 notNull
        if (revoked) {
            revokedCondition = AUTH_REFRESH_TOKEN.REVOKED_AT.isNotNull();
        } else {
            // 활성화된 토큰이라면 REVOKED_AT 는 null
            revokedCondition = AUTH_REFRESH_TOKEN.REVOKED_AT.isNull();
        }

        // 토큰이 존재하고, 활성화된 토큰인지 조회한다.
        return dsl.fetchExists(
            dsl.selectFrom(AUTH_REFRESH_TOKEN)
                .where(AUTH_REFRESH_TOKEN.TOKEN.eq(token))
                .and(revokedCondition)
        );
    }
}
