package net.detalk.api.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import net.detalk.api.domain.MemberProfile;

import static net.detalk.jooq.Tables.MEMBER_PROFILE;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberProfileRepositoryImpl implements MemberProfileRepository {
    private final DSLContext dsl;

    @Override
    public MemberProfile save(MemberProfile memberProfile) {
        return dsl.insertInto(MEMBER_PROFILE)
                .set(MEMBER_PROFILE.MEMBER_ID, memberProfile.getMemberId())
                .set(MEMBER_PROFILE.AVATAR_ID, memberProfile.getAvatarId())
                .set(MEMBER_PROFILE.USERHANDLE, memberProfile.getUserhandle())
                .set(MEMBER_PROFILE.NICKNAME, memberProfile.getNickname())
                .set(MEMBER_PROFILE.DESCRIPTION, memberProfile.getDescription())
                .set(MEMBER_PROFILE.UPDATED_AT, memberProfile.getUpdatedAt())
                .returning()
                .fetchOneInto(MemberProfile.class);
    }
}