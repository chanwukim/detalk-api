package net.detalk.api.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.LoginType;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberExternal;
import net.detalk.api.domain.MemberProfile;
import net.detalk.api.domain.MemberStatus;
import net.detalk.api.repository.MemberExternalRepository;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.security.OAuthProvider;
import net.detalk.api.support.security.OAuthUser;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.util.StringUtil;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionAuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberExternalRepository memberExternalRepository;
    private final TimeHolder timeHolder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerId = extractProviderId(provider, oAuth2User);
        log.debug("[loadUser] provider: {}, providerId: {}", provider, providerId);

        MemberExternal memberExternal = memberExternalRepository
            .findByTypeAndUid(provider, providerId)
            .orElseGet(() -> register(provider, providerId));

        List<String> authorities = List.of(SecurityRole.MEMBER.getName());

        return OAuthUser.builder()
            .id(memberExternal.getMemberId())
            .username("username")
            .authorities(AuthorityUtils.createAuthorityList(authorities.toArray(String[]::new)))
            .attributes(oAuth2User.getAttributes())
            .build();
    }

    private String extractProviderId(OAuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GOOGLE -> user.getAttribute("sub");
        };
    }

    private MemberExternal register(OAuthProvider provider, String providerId) {
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

        memberProfileRepository.save(
            MemberProfile.builder()
                .memberId(member.getId())
                .userhandle(StringUtil.generateMixedCaseAndNumber(64))
                .updatedAt(now)
                .build());

        return memberExternalRepository.save(
            MemberExternal.builder()
                .memberId(member.getId())
                .oauthProvider(provider)
                .uid(providerId)
                .createdAt(timeHolder.now())
                .build());
    }
}