package net.detalk.api.service;

import jakarta.transaction.Transactional;
import net.detalk.api.domain.*;
import net.detalk.api.repository.AuthRefreshTokenRepository;
import net.detalk.api.repository.MemberExternalRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.security.*;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.AuthorityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final MemberExternalRepository memberExternalRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final TimeHolder timeHolder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = extractProviderId(provider, user);
        log.debug("[loadUser] provider: {}", provider);
        log.debug("[loadUser] providerId: {}", providerId);

        MemberExternal memberExternal = memberExternalRepository.findByTypeAndUid(provider, providerId)
                .orElseGet(() -> register(provider, providerId));

        AccessToken accessToken = tokenProvider.createAccessToken(memberExternal.getMemberId());
        RefreshToken refreshToken = tokenProvider.createRefreshToken();

        authRefreshTokenRepository.save(
            AuthRefreshToken.builder()
                .memberId(memberExternal.getMemberId())
                .token(refreshToken.getValue())
                .createdAt(refreshToken.getIssuedAt().toInstant())
                .expiresAt(refreshToken.getExpiresAt().toInstant())
                .build()
        );

        return OAuthUser.builder()
                .id(memberExternal.getMemberId())
                .username("username")
                .accessToken(accessToken.getValue())
                .refreshToken(refreshToken.getValue())
                .authorities(AuthorityUtils.createAuthorityList(MemberRole.MEMBER.getName()))
                .attributes(user.getAttributes())
                .build();
    }

    private String extractProviderId(String provider, OAuth2User user) {
        return switch (OAuthProvider.valueOf(provider.toUpperCase())) {
            case GOOGLE -> user.getAttribute("sub");
            default -> {
                log.error("[extractProviderId] 알 수 없는 provider: {}", provider);
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private MemberExternal register(String provider, String providerId) {
        log.info("[register] 새 소셜회원가입 provider {}", provider);

        // 소셜 로그인후 LoginType.EXTERNAL, 상태는 PENDING이라면, 가입 form으로 이동
        Member member = memberRepository.save(
                Member.builder()
                        .loginType(LoginType.EXTERNAL)
                        .status(MemberStatus.PENDING)
                        .createdAt(timeHolder.now())
                        .updatedAt(timeHolder.now())
                        .build());

        return memberExternalRepository.save(
                MemberExternal.builder()
                        .memberId(member.getId())
                        .oauthProvider(OAuthProvider.valueOf(provider.toUpperCase()))
                        .uid(providerId)
                        .createdAt(timeHolder.now())
                        .build());
    }
}
