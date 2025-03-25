package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.security.SecurityUser;
import net.detalk.api.support.util.TimeHolder;
import net.detalk.api.support.util.UUIDGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 * 토큰 생성, 검증, 사용자 인증 정보 추출 기능 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class SystemJwtTokenProvider implements JwtTokenProvider{

    private final JwtConstants jwtConstants;
    private final JwtParserHolder jwtParserHolder;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    /**
     * AccessToken 생성
     * @return 생성된 JWT 토큰
     */
    public String generateAccessToken(Long memberId, String authorities) {

        Instant now = timeHolder.now();
        Instant validity = now.plusSeconds(jwtConstants.getAccessTokenValidity());

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("auth", authorities)
            .issuedAt(Date.from(now))
            .expiration(Date.from(validity))
            .signWith(jwtConstants.jwtSecretKey())
            .compact();
    }

    /**
     * JWT 토큰에서 인증 정보 추출
     * @param token JWT 토큰
     * @return Spring Security Authentication 객체
     */
    public Authentication getAuthentication(String token) {

        Claims claims = getClaimsByToken(token);

        var authorities = Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();

        SecurityUser securityUser = new SecurityUser(Long.parseLong(claims.getSubject()),
            authorities);

        return new UsernamePasswordAuthenticationToken(securityUser, token, authorities);
    }

    /**
     * AccessToken 검증
     * @param token 검증할 JWT 토큰
     * @throws JwtTokenException 만료된 토큰, 그 외
     */
    public void validateAccessToken(String token) {
        try {
            getClaimsByToken(token);
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new JwtTokenException(ErrorCode.ACCESS_TOKEN_INVALID);
        }
    }

    public Claims getClaimsByToken(String token) {
        return jwtParserHolder.parseSignedClaims(token);
    }

    public String generateRefreshToken(Long memberId, Instant now) {

        Instant validity = now.plusSeconds(jwtConstants.getRefreshTokenValidity());

        String randomId = uuidGenerator.generateV4().toString();

        return Jwts.builder()
            .id(randomId)
            .subject(String.valueOf(memberId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(validity))
            .signWith(jwtConstants.jwtSecretKey())
            .compact();
    }

    /**
     * RefreshToken 검증
     * @param token 검증할 JWT 토큰
     * @throws JwtTokenException 만료된 토큰, 그 외
     */
    public void validateRefreshToken(String token) {
        try {
            getClaimsByToken(token);
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new JwtTokenException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

}
