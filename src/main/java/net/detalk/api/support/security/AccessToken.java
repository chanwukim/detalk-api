package net.detalk.api.support.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class AccessToken {
    /**
     * JWT
     */
    private String value;
    private Long memberId;
    /**
     * 유저 역할 ROLE_MEMBER 등
     */
    private List<String> authorities;
    private Date issuedAt;
    private Date expiresAt;

    @Builder
    public AccessToken(String value, Long memberId, List<String> authorities, Date issuedAt, Date expiresAt) {
        this.value = value;
        this.memberId = memberId;
        this.authorities = authorities;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
