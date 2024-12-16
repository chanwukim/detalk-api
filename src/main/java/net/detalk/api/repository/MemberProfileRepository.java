package net.detalk.api.repository;

import net.detalk.api.domain.MemberProfile;

import java.util.Optional;

public interface MemberProfileRepository {
    MemberProfile save(MemberProfile memberProfile);
    Optional<MemberProfile> findByMemberId(Long memberId);
}
