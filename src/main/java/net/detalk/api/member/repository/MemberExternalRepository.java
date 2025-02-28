package net.detalk.api.member.repository;

import net.detalk.api.member.domain.MemberExternal;
import net.detalk.api.support.security.oauth.OAuthProvider;

import java.util.Optional;

public interface MemberExternalRepository {
    MemberExternal save(MemberExternal memberExternal);
    Optional<MemberExternal> findByTypeAndUid(OAuthProvider type, String uid);
}
