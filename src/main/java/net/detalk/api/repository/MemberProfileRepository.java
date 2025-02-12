package net.detalk.api.repository;

import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberProfile;

import java.util.Optional;

public interface MemberProfileRepository {
    MemberProfile save(MemberProfile memberProfile);
    Optional<MemberProfile> findByMemberId(Long memberId);
    Optional<MemberDetail> findWithAvatarByMemberId(Long memberId);
    Optional<MemberProfile> findByUserHandle(String handle);
    MemberProfile update(MemberProfile memberProfile);
    boolean existsByUserHandle(String userHandle);
}
