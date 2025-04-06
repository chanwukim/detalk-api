package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.Claims;
import java.time.Instant;
import org.springframework.security.core.Authentication;

public interface JwtTokenProvider {
    String generateAccessToken(Long memberId, String authorities);
    Authentication getAuthentication(String token);
    void validateAccessToken(String token);
    Claims getClaimsByToken(String token);
    String generateRefreshToken(Long memberId, Instant now);
    void validateRefreshToken(String token);
}
