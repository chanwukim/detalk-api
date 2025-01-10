package net.detalk.api.service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import net.detalk.api.domain.*;
import net.detalk.api.domain.exception.MemberNotFoundException;
import net.detalk.api.domain.exception.ProviderUnsupportedException;
import net.detalk.api.domain.exception.RefreshTokenNotFoundException;
import net.detalk.api.repository.AttachmentFileRepository;
import net.detalk.api.repository.AuthRefreshTokenRepository;
import net.detalk.api.repository.MemberExternalRepository;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.security.*;

import net.detalk.api.support.security.oauth.OAuthProvider;
import net.detalk.api.support.security.oauth.OAuthUser;
import net.detalk.api.support.util.StringUtil;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.AuthorityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberExternalRepository memberExternalRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final AttachmentFileRepository fileRepository;
    private final TokenProvider tokenProvider;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuthProvider 검증 및 변환
        // 시큐리티에서 이미 지원하지 않는 건 OAuth2AuthorizationRequestRedirectFilter에서
        // OAuth2AuthorizationRequestException 발생하긴 함
        OAuthProvider provider = validateAndGetProvider(userRequest.getClientRegistration().getRegistrationId());

        OAuth2User user = super.loadUser(userRequest);
        String providerId = extractProviderId(provider, user);
        String pictureUrl = user.getAttribute("picture");

        log.debug("[loadUser] provider: {}, providerId: {}", provider, providerId);

        // 소셜 계정 정보 조회 또는 신규 회원 등록
        MemberExternal memberExternal = memberExternalRepository
            .findByTypeAndUid(provider, providerId)
            .orElseGet(() -> register(provider, providerId, pictureUrl));

        // member 첫 회원가입 상태여부
        boolean isNewMember = memberRepository.findById(memberExternal.getMemberId())
            .orElseThrow(MemberNotFoundException::new)
            .isNewMember();

        // TODO: ADMIN 권한 확인
        List<String> authorities = List.of(SecurityRole.MEMBER.getName());
        AccessToken accessToken = tokenProvider.createAccessToken(memberExternal.getMemberId(), authorities);
        RefreshToken refreshToken = tokenProvider.createRefreshToken();

        authRefreshTokenRepository.save(
            AuthRefreshToken.builder()
                .memberId(memberExternal.getMemberId())
                .token(refreshToken.getValue())
                .createdAt(refreshToken.getIssuedAt().toInstant())
                .expiresAt(refreshToken.getExpiresAt().toInstant())
                .build());

        return OAuthUser.builder()
            .id(memberExternal.getMemberId())
            .username("username")
            .isNew(isNewMember)
            .accessToken(accessToken.getValue())
            .refreshToken(refreshToken.getValue())
            .authorities(AuthorityUtils.createAuthorityList(authorities.toArray(String[]::new)))
            .attributes(user.getAttributes())
            .build();
    }

    private OAuthProvider validateAndGetProvider(String registrationId) {
        try {
            return OAuthProvider.valueOf(registrationId.toUpperCase());
        } catch (Exception e) {
            log.error("[validateAndGetProvider] 알 수 없는 OAuth Provider: {}", registrationId);
            throw new ProviderUnsupportedException(registrationId);
        }
    }

    private String extractProviderId(OAuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GOOGLE -> user.getAttribute("sub");
        };
    }

    private MemberExternal register(OAuthProvider provider, String providerId,String pictureUrl) {
        log.info("[register] 새 소셜회원가입 provider {}", provider);

        Instant now = Instant.now();

        // 소셜 로그인후 LoginType.EXTERNAL, 상태는 PENDING이라면, 가입 form으로 이동
        Member member = memberRepository.save(
            Member.builder()
                .loginType(LoginType.EXTERNAL)
                .status(MemberStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build());

        UUID avatarId = null;
        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            avatarId = uuidGenerator.generateV7();
            String randomFileName = String.valueOf(uuidGenerator.generateV7());
            fileRepository.save(
                AttachmentFile.builder()
                    .id(avatarId)
                    .uploaderId(member.getId())
                    .name(randomFileName)
                    .extension(null)
                    .url(pictureUrl)
                    .createdAt(timeHolder.now())
                    .build()
            );
        }


        memberProfileRepository.save(
            MemberProfile.builder()
                .memberId(member.getId())
                .userhandle(StringUtil.generateMixedCaseAndNumber(64))
                .updatedAt(now)
                .avatarId(avatarId)
                .build());

        return memberExternalRepository.save(
            MemberExternal.builder()
                .memberId(member.getId())
                .oauthProvider(provider)
                .uid(providerId)
                .createdAt(timeHolder.now())
                .build());
    }

    @Transactional
    public AuthToken refresh(String originalRefreshToken) {
        RefreshToken verifiedRefreshToken = tokenProvider.parseRefreshToken(originalRefreshToken);
        AuthRefreshToken authRefreshToken = authRefreshTokenRepository.findByToken(verifiedRefreshToken.getValue())
            .orElseThrow(() -> {
                log.error("[refresh] 서버에 존재하지 않는 토큰 : {}", originalRefreshToken);
                return new RefreshTokenNotFoundException();
            });

        Long memberId = authRefreshToken.getMemberId();

        // TODO: ADMIN 권한 확인
        List<String> authorities = new ArrayList<>();
        authorities.add(SecurityRole.MEMBER.getName());

        AccessToken accessToken = tokenProvider.createAccessToken(memberId, authorities);
        RefreshToken refreshToken = tokenProvider.createRefreshToken();

        log.debug("[refresh] 기존 refresh 토큰 무효화 후 새 refresh 저장");
        authRefreshToken.revoked(timeHolder);
        authRefreshTokenRepository.update(authRefreshToken);

        authRefreshTokenRepository.save(
            AuthRefreshToken.builder()
                .memberId(memberId)
                .token(refreshToken.getValue())
                .createdAt(refreshToken.getIssuedAt().toInstant())
                .expiresAt(refreshToken.getExpiresAt().toInstant())
                .build());

        return new AuthToken(accessToken.getValue(), refreshToken.getValue());
    }

    public void signOut(String refreshToken) {
        RefreshToken verifiedRefreshToken = tokenProvider.parseRefreshToken(refreshToken);
        AuthRefreshToken authRefreshToken = authRefreshTokenRepository.findByToken(verifiedRefreshToken.getValue())
            .orElseThrow(() -> {
                log.error("[signOut] 서버에 존재하지 않는 토큰 : {}", refreshToken);
                return new RefreshTokenNotFoundException();
            });
        authRefreshToken.revoked(timeHolder);
        authRefreshTokenRepository.update(authRefreshToken);
    }
}
