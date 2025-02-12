package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.controller.v1.request.UpdateProfileRequest;
import net.detalk.api.controller.v1.response.GetMemberPublicProfileResponse;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberProfile;
import net.detalk.api.domain.exception.InvalidMemberStatusException;
import net.detalk.api.domain.exception.MemberNeedSignUpException;
import net.detalk.api.domain.exception.MemberNotFoundException;
import net.detalk.api.domain.exception.MemberProfileNotFoundException;
import net.detalk.api.domain.exception.UserHandleDuplicatedException;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    /**
     * 자신 프로필 조회
     */
    public MemberDetail me(Long memberId) {
        Member member = getMemberById(memberId);

        if (member.isPendingExternalMember()) {
            log.debug("[me] 회원가입이 필요한 외부 회원");
            throw new MemberNeedSignUpException(memberId, member.getLoginType(),
                member.getStatus());
        }

        return getMemberDetailByMemberId(member.getId());
    }

    /**
     * 프로필 생성
     */
    @Transactional
    public MemberDetail registerProfile(Long memberId, String userhandle, String nickname) {
        log.debug("[registerProfile] userhandle 중복검사 {}", userhandle);
        memberProfileRepository.findByUserHandle(userhandle).ifPresent(m -> {
            log.error("[registerProfile] 이미 존재하는 userhandle({}) 입니다.", userhandle);
            throw new UserHandleDuplicatedException(userhandle);
        });

        Member member = getMemberById(memberId);

        if (!member.isPendingExternalMember()) {
            throw new InvalidMemberStatusException(memberId, member.getStatus());
        }

        member.active(timeHolder);
        memberRepository.update(member);
        MemberProfile memberProfile = getProfileByMemberId(member.getId());

         memberProfile = memberProfileRepository.update(
            MemberProfile.builder()
                .id(memberProfile.getId())
                .memberId(member.getId())
                .userhandle(userhandle)
                .nickname(nickname)
                .avatarId(memberProfile.getAvatarId())
                .updatedAt(timeHolder.now())
                .build()
        );

        return MemberDetail.builder()
            .id(member.getId())
            .userhandle(memberProfile.getUserhandle())
            .nickname(memberProfile.getNickname())
            .description(memberProfile.getDescription())
            .build();
    }

    /**
     * 프로필 업데이트
     */
    @Transactional
    public void updateProfile(Long memberId, UpdateProfileRequest updateRequest) {

        Member member = getMemberById(memberId);
        MemberProfile memberProfile = getProfileByMemberId(member.getId());

        /**
         * 새로운 userHandle 요청이라면, 이미 존재하는지 검사한다.
         */
        if (!memberProfile.hasSameUserHandle(updateRequest.userhandle())) {
            checkDuplicateUserHandle(updateRequest.userhandle());
        }

        /**
         * avatarId는 null일 경우 기존꺼 사용
         * 새로 요청올 경우, 새거 사용
         */
        memberProfile = memberProfile.update(
            updateRequest,
            updateRequest.avatarId() != null
                ? uuidGenerator.fromString(updateRequest.avatarId())
                : memberProfile.getAvatarId(),
            timeHolder.now()
        );

        memberProfileRepository.update(memberProfile);
    }

    /**
     * userhandle로 회원 프로필 조회
     */
    @Transactional(readOnly = true)
    public GetMemberPublicProfileResponse getMemberDetailByUserhandle(String userhandle) {

        MemberProfile memberProfile = getProfileByUserhandle(userhandle);

        MemberDetail memberDetail = getMemberDetailByMemberId(memberProfile.getMemberId());

        return GetMemberPublicProfileResponse.builder()
            .userhandle(memberProfile.getUserhandle())
            .nickname(memberProfile.getNickname())
            .description(memberProfile.getDescription())
            .avatarUrl(memberDetail.getAvatarUrl())
            .build();
    }

    /**
     * MemberId로 회원 조회
     */
    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> {
            log.error("[findMemberById] 회원 ID {}는 존재하지 않습니다", id);
            return new MemberNotFoundException();
        });
    }

    /**
     * UserHandle로 MemberId 조회
     */
    public Long getMemberIdByUserHandle(String userHandle) {
        return getProfileByUserhandle(userHandle).getId();
    }

    /**
     * Userhandle로 회원 프로필 조회
     */
    public MemberProfile getProfileByUserhandle(String userhandle) {
        return memberProfileRepository.findByUserHandle(userhandle)
            .orElseThrow(() -> {
                    log.error("[findMemberIdByUserHandle] 회원 userHandle {}은 존재하지 않는 회원입니다", userhandle);
                    return new MemberProfileNotFoundException(userhandle);
                }
            );
    }

    /**
     * MemberId 로 회원 프로필 조회
     */
    public MemberProfile getProfileByMemberId(Long memberId) {
        return memberProfileRepository.findByMemberId(memberId)
            .orElseThrow(()->{
                log.error("[findProfileByMemberId] 존재하지 않는 회원 프로필 입니다. memberId={}", memberId);
                return new MemberProfileNotFoundException();
            });
    }

    /**
     * MemberId로 회원 프로필 모든 정보 조회 (avatarUrl 포함)
     */
    public MemberDetail getMemberDetailByMemberId(Long memberId) {
        return memberProfileRepository.findWithAvatarByMemberId(memberId)
            .orElseThrow(() -> {
                log.error("[GetMemberPublicProfileResponse] 존재하지 않는 회원 프로필 입니다. memberId={}",
                    memberId);
                return new MemberProfileNotFoundException();
            });
    }

    /**
     * UserHandle 중복 검증
     */
    public void checkDuplicateUserHandle(String userHandle) {
        if (memberProfileRepository.existsByUserHandle(userHandle)) {
            log.info("[duplicateUserHandleValidation] 이미 존재하는 userhandle입니다. userhandle={}",
                userHandle);
            throw new UserHandleDuplicatedException(userHandle);
        }
    }
}
