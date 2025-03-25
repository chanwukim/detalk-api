package net.detalk.api.support.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.mock.FakeJwtConstants;
import net.detalk.api.mock.FakeJwtParser;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class SystemJwtTokenProviderTest {

    private SystemJwtTokenProvider jwtTokenProvider;
    private FakeJwtConstants jwtConstants;
    private FakeJwtParser jwtParser;
    private FakeTimeHolder timeHolder;
    private FakeUUIDGenerator uuidGenerator;

    private final String TEST_SECRET_KEY = "testSecretKeyWithAtLeast32Characters123456789012345678901234567890";
    private final Instant FIXED_TIME = Instant.parse("2025-01-01T12:00:00Z");
    private final LocalDateTime FIXED_LOCAL_DATE_TIME = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    private final UUID FIXED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final String TEST_TOKEN = "test.jwt.token";
    private final Long TEST_MEMBER_ID = 123L;

    @BeforeEach
    void setup() {
        // Fake 구현체 초기화
        jwtConstants = new FakeJwtConstants(TEST_SECRET_KEY, 3600, 86400, "/api/v2/auth/refresh","/");
        jwtParser = new FakeJwtParser();
        timeHolder = new FakeTimeHolder(FIXED_TIME, FIXED_LOCAL_DATE_TIME);
        uuidGenerator = new FakeUUIDGenerator(FIXED_UUID);

        // 테스트에 필요한 Claim 설정
        Claims testClaims = Jwts.claims()
            .subject(TEST_MEMBER_ID.toString())
            .add("auth", "ROLE_USER")
            .build();

        // FakeJwtParser에 토큰과 Claims 매핑 설정
        jwtParser.setTokenClaims(TEST_TOKEN, testClaims);
        jwtParser.setTokenSubject(TEST_TOKEN, TEST_MEMBER_ID.toString());

        jwtTokenProvider = new SystemJwtTokenProvider(
            jwtConstants,
            jwtParser,
            timeHolder,
            uuidGenerator
        );
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void testGenerateAccessToken() {

        // given
        String authorities = "ROLE_USER";

        // when
        String token = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, authorities);

        // then
        assertThat(token).isNotNull();
        assertThat(token.length()).isGreaterThan(20);
    }
    @Test
    @DisplayName("인증 정보 추출 성공")
    void testGetAuthentication() {
        // when
        Authentication auth = jwtTokenProvider.getAuthentication(TEST_TOKEN);

        SecurityUser principal = (SecurityUser) auth.getPrincipal();

        // then
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isEqualTo(true);
        assertThat(TEST_MEMBER_ID).isEqualTo(principal.getId());
        assertThat(auth.getAuthorities().size()).isOne();
        assertThat(auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")))
            .isTrue();

    }

    @Test
    @DisplayName("만료된 액세스 토큰 검증 성공")
    void testValidateExpiredAccessToken() {

        FakeJwtParser expiredTokenParser = new FakeJwtParser(new ExpiredJwtException(null, null, "Token expired"));

        SystemJwtTokenProvider provider = new SystemJwtTokenProvider(jwtConstants, expiredTokenParser, timeHolder, uuidGenerator);

        assertThatThrownBy(() -> provider.validateAccessToken(TEST_TOKEN))
            .isInstanceOf(JwtTokenException.class)
            .hasMessage(ErrorCode.ACCESS_TOKEN_EXPIRED.getMessage());

    }

    @Test
    @DisplayName("INVALID 액세스 토큰 검증 성공")
    void testValidateInvalidToken() {

        // given
        FakeJwtParser expiredTokenParser = new FakeJwtParser(new RuntimeException("Token invalid"));

        SystemJwtTokenProvider provider = new SystemJwtTokenProvider(jwtConstants, expiredTokenParser, timeHolder, uuidGenerator);

        // when & then
        assertThatThrownBy(() -> provider.validateAccessToken(TEST_TOKEN))
            .isInstanceOf(JwtTokenException.class)
            .hasMessage(ErrorCode.ACCESS_TOKEN_INVALID.getMessage());
    }

    @Test
    @DisplayName("토큰으로 클레임 추출 성공")
    void testGetClaimsByToken() {

        // when
        Claims claims = jwtTokenProvider.getClaimsByToken(TEST_TOKEN);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(TEST_MEMBER_ID.toString());
        assertThat(claims.get("auth")).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void testGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(TEST_MEMBER_ID, FIXED_TIME);

        // then
        assertThat(token).isNotNull();
        assertThat(token.length()).isGreaterThan(20);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰 검증 성공")
    void testValidateExpiredRefreshToken() {

        FakeJwtParser expiredTokenParser = new FakeJwtParser(new ExpiredJwtException(null, null, "Token expired"));

        SystemJwtTokenProvider provider = new SystemJwtTokenProvider(jwtConstants, expiredTokenParser, timeHolder, uuidGenerator);

        assertThatThrownBy(() -> provider.validateRefreshToken(TEST_TOKEN))
            .isInstanceOf(JwtTokenException.class)
            .hasMessage(ErrorCode.REFRESH_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("INVALID 리프레시 토큰 검증 성공")
    void testValidateInvalidRefreshToken() {

        FakeJwtParser expiredTokenParser = new FakeJwtParser(new RuntimeException("Token Invalid"));

        SystemJwtTokenProvider provider = new SystemJwtTokenProvider(jwtConstants, expiredTokenParser, timeHolder, uuidGenerator);

        assertThatThrownBy(() -> provider.validateRefreshToken(TEST_TOKEN))
            .isInstanceOf(JwtTokenException.class)
            .hasMessage(ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
    }
}