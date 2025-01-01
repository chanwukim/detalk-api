package net.detalk.api.repository;

import net.detalk.api.domain.MemberDetail;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import net.detalk.api.domain.MemberProfile;

import static net.detalk.jooq.Tables.MEMBER_PROFILE;
import static net.detalk.jooq.tables.JAttachmentFile.ATTACHMENT_FILE;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

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

    @Override
    public Optional<MemberProfile> findByMemberId(Long memberId) {
        return dsl.selectFrom(MEMBER_PROFILE)
            .where(MEMBER_PROFILE.MEMBER_ID.eq(memberId))
            .fetchOptionalInto(MemberProfile.class);
    }

    @Override
    public Optional<MemberDetail> findWithAvatarByMemberId(Long memberId) {
        return dsl.select(
                MEMBER_PROFILE.MEMBER_ID.as("id"),
                MEMBER_PROFILE.USERHANDLE.as("userhandle"),
                MEMBER_PROFILE.NICKNAME.as("nickname"),
                MEMBER_PROFILE.DESCRIPTION.as("description"),
                ATTACHMENT_FILE.URL.as("avatarUrl")
            )
            .from(MEMBER_PROFILE)
            .leftJoin(ATTACHMENT_FILE)
            .on(MEMBER_PROFILE.AVATAR_ID.eq(ATTACHMENT_FILE.ID))
            .where(MEMBER_PROFILE.MEMBER_ID.eq(memberId))
            .fetchOptionalInto(MemberDetail.class);
    }

    @Override
    public Optional<MemberProfile> findByUserHandle(String handle) {
        return dsl.selectFrom(MEMBER_PROFILE)
            .where(MEMBER_PROFILE.USERHANDLE.eq(handle))
            .fetchOptionalInto(MemberProfile.class);
    }

    @Override
    public MemberProfile update(MemberProfile memberProfile) {
        return dsl.update(MEMBER_PROFILE)
            .set(MEMBER_PROFILE.AVATAR_ID, memberProfile.getAvatarId())
            .set(MEMBER_PROFILE.USERHANDLE, memberProfile.getUserhandle())
            .set(MEMBER_PROFILE.NICKNAME, memberProfile.getNickname())
            .set(MEMBER_PROFILE.DESCRIPTION, memberProfile.getDescription())
            .set(MEMBER_PROFILE.UPDATED_AT, memberProfile.getUpdatedAt())
            .where(MEMBER_PROFILE.ID.eq(memberProfile.getId()))
            .returning()
            .fetchOneInto(MemberProfile.class);
    }
}
