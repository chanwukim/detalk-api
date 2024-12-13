package net.detalk.api.support.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.AppProperties;
import net.detalk.api.support.error.ExpiredTokenException;
import net.detalk.api.support.error.TokenException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Component
public class TokenProvider {
    private static final String KEY_CLAIMS_ID = "id";
    private final AppProperties appProperties;
    private final SecretKey secretKey;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        byte[] keyBytes = Base64.getEncoder().encode(appProperties.getTokenSecret().getBytes());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public AccessToken createAccessToken(Long memberId) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime()
            + appProperties.getAccessTokenExpiresInSeconds() * 1000L);

        Claims claims = Jwts.claims()
            .add(KEY_CLAIMS_ID, memberId)
            .issuedAt(issuedAt)
            .expiration(expiresAt)
            .build();

        String token = createToken(claims);

        return AccessToken.builder()
            .value(token)
            .memberId(memberId)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();
    }

    public RefreshToken createRefreshToken() {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime()
            + appProperties.getRefreshTokenExpiresInSeconds() * 1000L);

        Claims claims = Jwts.claims()
            .issuedAt(issuedAt)
            .expiration(expiresAt)
            .build();

        String token = createToken(claims);

        return RefreshToken.builder()
            .value(token)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();
    }

    private String createToken(Claims claims) {
        if (claims.getIssuedAt() == null) {
            log.error("[createToken] claims에 issuedAt 정보가 없습니다.");
            throw new TokenException("Claims must have issuedAt");
        }
        if (claims.getExpiration() == null) {
            log.error("[createToken] claims에 expiration 정보가 없습니다.");
            throw new TokenException("Claims must have expiration");
        };

        return Jwts.builder()
            .claims(claims)
            .issuedAt(claims.getIssuedAt())
            .expiration(claims.getExpiration())
            .signWith(secretKey)
            .compact();
    }

    public AccessToken parseAccessToken(String token) {
        try {
            Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return AccessToken.builder()
                .value(token)
                .memberId(payload.get(KEY_CLAIMS_ID, Long.class))
                .issuedAt(payload.getIssuedAt())
                .expiresAt(payload.getExpiration())
                .build();
        } catch (ExpiredJwtException e) {
            log.warn("[parseAccessToken] 만료된 액세스 토큰 : {}", token);
            throw new ExpiredTokenException();
        } catch (Exception e) {
            log.warn("[parseAccessToken] {}", e.getMessage());
            throw new TokenException("Invalid token");
        }
    }

    public RefreshToken parseRefreshToken(String token) {
        try {
            Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return RefreshToken.builder()
                .value(token)
                .issuedAt(payload.getIssuedAt())
                .expiresAt(payload.getExpiration())
                .build();
        } catch (ExpiredJwtException e) {
            log.warn("[parseRefreshToken] 만료된 리프레시 토큰 : {}", token);
            throw new ExpiredTokenException();
        } catch (Exception e) {
            log.warn("[parseRefreshToken] {}", e.getMessage());
            throw new TokenException("Invalid token");
        }
    }
}
