package net.detalk.api.member.repository;

import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.member.domain.MemberProfile;

import java.util.Optional;

public interface MemberProfileRepository {
    MemberProfile save(MemberProfile memberProfile);
    Optional<MemberProfile> findByMemberId(Long memberId);
    Optional<GetMemberProfileResponse> findWithAvatarByMemberId(Long memberId);
    Optional<MemberProfile> findByUserHandle(String handle);
    MemberProfile update(MemberProfile memberProfile);
    boolean existsByUserHandle(String userHandle);
}
