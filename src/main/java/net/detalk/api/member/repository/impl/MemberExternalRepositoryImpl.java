package net.detalk.api.member.repository.impl;

import net.detalk.api.member.domain.MemberExternal;
import net.detalk.api.member.repository.MemberExternalRepository;
import net.detalk.api.support.security.oauth.OAuthProvider;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static net.detalk.jooq.Tables.MEMBER_EXTERNAL;

@Repository
@RequiredArgsConstructor
public class MemberExternalRepositoryImpl implements MemberExternalRepository {
    private final DSLContext dsl;

    @Override
    public MemberExternal save(MemberExternal memberExternal) {
        return dsl.insertInto(MEMBER_EXTERNAL)
                .set(MEMBER_EXTERNAL.MEMBER_ID, memberExternal.getMemberId())
                .set(MEMBER_EXTERNAL.TYPE, memberExternal.getOauthProvider())
                .set(MEMBER_EXTERNAL.UID, memberExternal.getUid())
                .set(MEMBER_EXTERNAL.CREATED_AT, memberExternal.getCreatedAt())
                .returning()
                .fetchOneInto(MemberExternal.class);
    }

    @Override
    public Optional<MemberExternal> findByTypeAndUid(OAuthProvider type, String uid) {
        return dsl
                .selectFrom(MEMBER_EXTERNAL)
                .where(MEMBER_EXTERNAL.TYPE.eq(type))
                .and(MEMBER_EXTERNAL.UID.eq(uid))
                .fetchOptionalInto(MemberExternal.class);
    }
}
