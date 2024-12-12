package net.detalk.api.support.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
public class AccessToken {
    private Long memberId;
    private Date issuedAt;
    private Date expiresAt;

    @Builder
    public AccessToken(Long memberId, Date issuedAt, Date expiresAt) {
        this.memberId = memberId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
