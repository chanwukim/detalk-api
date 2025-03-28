package net.detalk.api.auth.service;

import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.controller.v2.response.JwtTokenResponse;
import net.detalk.api.auth.domain.RefreshToken;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.auth.repository.RefreshTokenRepository;
import net.detalk.api.member.repository.MemberRoleRepository;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.security.jwt.JwtConstants;
import net.detalk.api.support.security.jwt.JwtTokenProvider;
import net.detalk.api.support.util.TimeHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtConstants jwtConstants;
    private final TimeHolder timeHolder;

    /**
     * 리프레시 토큰 검증 후 새로운 액세스 토큰과 리프레시 토큰 발급
     *
     * @param oldRefreshToken 기존 리프레시 토큰
     * @return 새롭게 발급된 액세스 토큰과 리프레시 토큰 포함한 응답 객체
     */
    public JwtTokenResponse refreshAccessToken(String oldRefreshToken) {

        // 토큰 검증
        tokenProvider.validateRefreshToken(oldRefreshToken);

        // DB에서 활성화된 토큰인지 확인
        boolean activeToken = refreshTokenRepository.isActiveToken(oldRefreshToken, false);

        if (!activeToken) {
            log.debug("비활성화된 리프레시 토큰 입니다.");
            throw new JwtTokenException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 토큰에서 사용자 ID 추출
        Claims claims = tokenProvider.getClaimsByToken(oldRefreshToken);
        Long memberId = Long.valueOf(claims.getSubject());

        // 새 토큰 생성
        List<String> memberRoles = memberRoleRepository.findRolesByMemberId(memberId);
        String newRefreshToken = createRefreshToken(memberId);
        String newAccessToken = tokenProvider.generateAccessToken(memberId,
            String.join(",", memberRoles));


        return new JwtTokenResponse(newAccessToken, newRefreshToken);
    }


    /**
     * 기존 리프레시 토큰 비활성화 후 새로 생성
     * @param memberId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(Long memberId) {

        Instant now = timeHolder.now();

        String tokenValue = tokenProvider.generateRefreshToken(memberId, now);

        // 기존 토큰 비활성화
        refreshTokenRepository.revokeByMemberId(memberId, now);

        Instant expiresAt = now.plusSeconds(jwtConstants.getRefreshTokenValidity());

        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(memberId)
            .token(tokenValue)
            .createdAt(now)
            .expiresAt(expiresAt)
            .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    public void revokeRefreshToken(String refreshToken) {
        Claims claims = tokenProvider.getClaimsByToken(refreshToken);
        Long memberId = Long.valueOf(claims.getSubject());
        refreshTokenRepository.revokeByMemberId(memberId, timeHolder.now());
    }
}
