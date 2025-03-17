package net.detalk.api.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.controller.v1.response.GetSessionInfoResponse;
import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.member.domain.LoginType;
import net.detalk.api.member.domain.Member;
import net.detalk.api.member.domain.MemberExternal;
import net.detalk.api.member.domain.MemberProfile;
import net.detalk.api.member.domain.MemberStatus;
import net.detalk.api.member.domain.exception.MemberNotFoundException;
import net.detalk.api.role.domain.Role;
import net.detalk.api.member.domain.exception.MemberProfileNotFoundException;
import net.detalk.api.member.repository.MemberExternalRepository;
import net.detalk.api.member.repository.MemberProfileRepository;
import net.detalk.api.member.repository.MemberRepository;
import net.detalk.api.role.repository.RoleRepository;
import net.detalk.api.support.util.TimeHolder;
import net.detalk.api.support.security.oauth.OAuthProvider;
import net.detalk.api.support.security.oauth.CustomOAuth2User;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.util.StringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "session")
public class SessionOAuth2Service extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberExternalRepository memberExternalRepository;
    private final RoleRepository roleRepository;
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
            .orElseGet(() -> register(provider, providerId, timeHolder.now()));

        // 회원 권한 목록 조회
        List<Role> dbRoles = roleRepository.findRolesByMemberId(memberExternal.getMemberId());

        List<GrantedAuthority> securityRoles = dbRoles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getCode()))
            .collect(Collectors.toList());

        MemberProfile memberProfile = memberProfileRepository.findByMemberId(
                memberExternal.getMemberId())
            .orElseThrow(MemberProfileNotFoundException::new);

        return CustomOAuth2User.builder()
            .id(memberExternal.getMemberId())
            .username(memberProfile.getUserhandle())
            .authorities(securityRoles)
            .attributes(oAuth2User.getAttributes())
            .build();
    }

    @Transactional(readOnly = true)
    public GetSessionInfoResponse getSessionInfo(Long memberId) {

        Member member =  memberRepository.findById(memberId).orElseThrow(() -> {
            log.info("[getSessionInfo] 회원 ID {}는 존재하지 않습니다", memberId);
            return new MemberNotFoundException();
        });

        GetMemberProfileResponse memberProfileDto = memberProfileRepository.findWithAvatarByMemberId(memberId)
            .orElseThrow(() -> {
                log.info("[GetMemberPublicProfileResponse] 존재하지 않는 회원 프로필 입니다. memberId={}",
                    memberId);
                return new MemberProfileNotFoundException();
            });


        List<String> roles = member.isPendingExternalMember() ? List.of() : List.of("member");

        return GetSessionInfoResponse.builder()
            .id(memberId)
            .userhandle(memberProfileDto.userhandle())
            .nickname(memberProfileDto.nickname())
            .description(memberProfileDto.description())
            .avatarUrl(memberProfileDto.avatarUrl())
            .roles(roles)
            .build();
    }

    private String extractProviderId(OAuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GOOGLE -> user.getAttribute("sub");
        };
    }

    private MemberExternal register(OAuthProvider provider, String providerId, Instant now) {
        log.info("[register] 새 소셜회원가입 provider {}", provider);

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

        roleRepository.saveMemberRole(member.getId(), SecurityRole.MEMBER.getName());

        return memberExternalRepository.save(
            MemberExternal.builder()
                .memberId(member.getId())
                .oauthProvider(provider)
                .uid(providerId)
                .createdAt(now)
                .build());
    }
}
