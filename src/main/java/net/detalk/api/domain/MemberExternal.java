package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;
import net.detalk.api.support.security.oauth.OAuthProvider;

import java.time.Instant;

@Getter
public class MemberExternal {
    private Long id;
    private Long memberId;
    private OAuthProvider oauthProvider;
    private String uid;
    private Instant createdAt;

    @Builder
    public MemberExternal(Long id, Long memberId, OAuthProvider oauthProvider, String uid, Instant createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.oauthProvider = oauthProvider;
        this.uid = uid;
        this.createdAt = createdAt;
    }
}
