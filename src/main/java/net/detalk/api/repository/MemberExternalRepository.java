package net.detalk.api.repository;

import net.detalk.api.domain.MemberExternal;

import java.util.Optional;

public interface MemberExternalRepository {
    MemberExternal save(MemberExternal memberExternal);
    Optional<MemberExternal> findByTypeAndUid(String type, String uid);
}
