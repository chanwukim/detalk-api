package net.detalk.api.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberProfile;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.InvalidStateException;
import net.detalk.api.support.error.ErrorCode;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TimeHolder timeHolder;

    public MemberDetail me(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("[me] 회원 ID {}는 존재하지 않는 회원입니다", memberId);
            return new ApiException(ErrorCode.UNAUTHORIZED);
        });

        if (member.isPendingExternalMember()) {
            log.debug("[me] 회원가입이 필요한 외부 회원");
            throw new ApiException(ErrorCode.NEED_SIGN_UP);
        }

        return memberProfileRepository.findWithAvatarByMemberId(member.getId()).orElseThrow(
            () -> new InvalidStateException("[me] 회원 " + member.getId() + "의 프로필이 존재하지 않습니다")
        );
    }

    @Transactional
    public MemberDetail registerProfile(Long memberId, String userhandle, String nickname) {
        log.debug("[registerProfile] userhandle 중복검사 {}", userhandle);
        memberProfileRepository.findByUserHandle(userhandle).ifPresent(m -> {
            throw new ApiException(ErrorCode.CONFLICT);
        });

        Member member = memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("[registerProfile] 회원 ID {}는 존재하지 않는 회원입니다", memberId);
            return new ApiException(ErrorCode.UNAUTHORIZED);
        });

        if (!member.isPendingExternalMember()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        member.active(timeHolder);
        memberRepository.update(member);
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new InvalidStateException("[me] 회원 " + member.getId() + "의 프로필이 존재하지 않습니다"));

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

    public Long findIdByUserHandle(String userHandle) {
        MemberProfile memberProfile = memberProfileRepository.findByUserHandle(userHandle)
            .orElseThrow(() -> {
                    log.error("[findMemberIdByUserHandle] 회원 userHandle {}은 존재하지 않는 회원입니다", userHandle);
                    return new ApiException(ErrorCode.NOT_FOUND);
                }
            );
        return memberProfile.getMemberId();
    }
}
