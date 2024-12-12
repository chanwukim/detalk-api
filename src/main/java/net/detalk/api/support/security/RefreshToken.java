package net.detalk.api.support.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
public class RefreshToken {
    /**
     * JWT
     */
    private String value;
    private Date issuedAt;
    private Date expiresAt;

    @Builder
    public RefreshToken(String value, Date issuedAt, Date expiresAt) {
        this.value = value;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
