package net.detalk.api.support.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
public class AccessToken {
    /**
     * JWT
     */
    private String value;
    private Long memberId;
    private Date issuedAt;
    private Date expiresAt;

    @Builder
    public AccessToken(String value, Long memberId, Date issuedAt, Date expiresAt) {
        this.value = value;
        this.memberId = memberId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
