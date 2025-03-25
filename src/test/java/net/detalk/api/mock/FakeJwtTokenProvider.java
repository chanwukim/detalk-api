package net.detalk.api.mock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.detalk.api.support.security.SecurityUser;
import net.detalk.api.support.security.jwt.JwtTokenProvider;
import net.detalk.api.support.security.jwt.JwtConstants;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class FakeJwtTokenProvider implements JwtTokenProvider {

    private final JwtConstants jwtConstants;
    private final Map<String, Claims> claimsMap = new HashMap<>(); // 토큰별 클레임 저장
    private final AtomicLong tokenIdCounter = new AtomicLong(1); // 고유한 Refresh Token 생성을 위해


    //생성자를 통해 가짜 토큰값 주입
    public FakeJwtTokenProvider(JwtConstants jwtConstants) {
        this.jwtConstants = jwtConstants;
    }

    @Override
    public String generateAccessToken(Long memberId, String authorities) {
        String uniqueAccessToken = "fake_accessToken_token_" + tokenIdCounter.getAndIncrement();

        // 테스트용 클레임 생성 및 저장
        Claims claims = Jwts.claims()
            .subject(String.valueOf(memberId))
            .add("auth", authorities)
            .build();
        claimsMap.put(uniqueAccessToken, claims);
        return uniqueAccessToken;
    }

    @Override
    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsByToken(token);

        // Optional을 사용하여 auth 클레임 가져오기 및 처리
        String authClaim = Optional.ofNullable(claims.get("auth"))
            .map(Object::toString) // Object -> String
            .orElse("ROLE_USER");    // null 이면 "ROLE_USER"
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authClaim.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();

        // 클레임에서 subject(사용자 ID) 가져오기, 없으면 예외 발생
        Long memberId = Long.parseLong(claims.getSubject());

        SecurityUser securityUser = new SecurityUser(memberId, authorities);
        return new UsernamePasswordAuthenticationToken(securityUser, token, authorities);
    }

    @Override
    public void validateAccessToken(String token) {
    }

    @Override
    public Claims getClaimsByToken(String token) {
        return claimsMap.get(token);
    }

    @Override
    public String generateRefreshToken(Long memberId, Instant now) {
        // 매번 다른 Refresh Token 값 생성
        String uniqueRefreshToken = "fake_refresh_token_" + tokenIdCounter.getAndIncrement();

        // 테스트용 클레임 생성 및 저장
        Claims claims = Jwts.claims()
            .subject(String.valueOf(memberId))
            .id(UUID.randomUUID().toString()) //고유 refreshToken id
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(jwtConstants.getRefreshTokenValidity()))) //만료시간 설정
            .build();
        claimsMap.put(uniqueRefreshToken, claims);

        return uniqueRefreshToken;
    }


    @Override
    public void validateRefreshToken(String token) {

    }
}
