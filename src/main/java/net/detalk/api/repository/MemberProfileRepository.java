package net.detalk.api.repository;

import net.detalk.api.domain.MemberProfile;

public interface MemberProfileRepository {
    MemberProfile save(MemberProfile memberProfile);
}
