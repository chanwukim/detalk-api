package net.detalk.api.repository;

import net.detalk.api.domain.MemberExternal;
import net.detalk.api.support.security.OAuthProvider;

import java.util.Optional;

public interface MemberExternalRepository {
    MemberExternal save(MemberExternal memberExternal);
    Optional<MemberExternal> findByTypeAndUid(OAuthProvider type, String uid);
}
