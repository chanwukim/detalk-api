package net.detalk.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import net.detalk.api.auth.controller.v2.response.JwtTokenResponse;
import net.detalk.api.auth.domain.RefreshToken;
import net.detalk.api.auth.repository.RefreshTokenRepository;
import net.detalk.api.member.repository.MemberRoleRepository;
import net.detalk.api.mock.FakeJwtConstants;
import net.detalk.api.mock.FakeJwtTokenProvider;
import net.detalk.api.mock.FakeMemberRoleRepository;
import net.detalk.api.mock.FakeRefreshTokenRepository;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import net.detalk.api.support.security.jwt.JwtTokenProvider;
import net.detalk.api.support.security.jwt.JwtConstants;
import net.detalk.api.support.util.TimeHolder;
import net.detalk.api.support.util.UUIDGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;
    private RefreshTokenRepository refreshTokenRepository;
    private MemberRoleRepository memberRoleRepository;
    private JwtTokenProvider jwtTokenProvider;
    private JwtConstants jwtConstants;
    private TimeHolder timeHolder;
    private UUIDGenerator uuidGenerator;

    @BeforeEach
    void setup() {
        refreshTokenRepository = new FakeRefreshTokenRepository();
        memberRoleRepository = new FakeMemberRoleRepository();
        jwtConstants = new FakeJwtConstants(
            "testsecretkeytestsecretkeytestsecretkey", //secretKey
            3600, // accessTokenValidity
            86400, // refreshTokenValidity
            "/refresh",  // refreshPath
            "/access"
        );
        jwtTokenProvider = new FakeJwtTokenProvider(jwtConstants);
        timeHolder = new FakeTimeHolder(Instant.now(), LocalDateTime.now());
        uuidGenerator = new FakeUUIDGenerator(UUID.randomUUID());

        refreshTokenService = new RefreshTokenService(
            refreshTokenRepository,
            memberRoleRepository,
            jwtTokenProvider,
            jwtConstants,
            timeHolder
        );

    }

    @DisplayName("새로운 AccessToken과 RefreshToken 발급 성공")
    @Test
    void refreshAccessToken() {
        // Given
        Long memberId = 1L;

        String oldRefreshToken = jwtTokenProvider.generateRefreshToken(memberId, timeHolder.now());

        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(memberId)
            .token(oldRefreshToken)
            .createdAt(timeHolder.now())
            .expiresAt(timeHolder.now().plusSeconds(jwtConstants.getRefreshTokenValidity()))
            .revokedAt(null)
            .build();

        refreshTokenRepository.save(refreshToken);

        // When
        JwtTokenResponse response = refreshTokenService.refreshAccessToken(oldRefreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.refreshToken()).isNotEqualTo(oldRefreshToken);
    }

    @DisplayName("리프레시 토큰 생성 성공")
    @Test
    void createRefreshToken() {
        // given
        Long memberId = 1L;

        // when
        String newRefreshToken = refreshTokenService.createRefreshToken(memberId);

        // then
        assertThat(newRefreshToken).isNotNull();
    }


}

