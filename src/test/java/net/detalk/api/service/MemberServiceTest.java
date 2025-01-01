package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.domain.LoginType;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberStatus;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    /**
     * target test class
     */
    private MemberService memberService;

    /**
     * repository
     */
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberProfileRepository memberProfileRepository;

    /**
     * fake random classes
     */
    private TimeHolder timeHolder = new FakeTimeHolder(Instant.parse("2025-01-01T12:00:00Z"));

    /**
     * objects
     */
    private Member member;
    private MemberDetail memberDetail;
    private final Long memberId = 1L;
    private final Long memberProfileId = 1L;
    private String userHandle = "hello_handle";
    private String nickname = "hello_nickname";
    private String description = "hello_description";
    private String avatarUrl = "hello_avatar_url";

    @BeforeEach
    void init() {
        memberService = new MemberService(
            memberRepository,
            memberProfileRepository,
            timeHolder
        );

        member = Member.builder()
            .id(memberId)
            .loginType(LoginType.EXTERNAL)
            .status(MemberStatus.ACTIVE)
            .createdAt(timeHolder.now())
            .updatedAt(timeHolder.now())
            .deletedAt(null)
            .build();

        memberDetail = MemberDetail.builder()
            .id(memberProfileId)
            .userhandle(userHandle)
            .nickname(nickname)
            .description(description)
            .avatarUrl(avatarUrl)
            .build();
    }

    @DisplayName("성공[me]")
    @Test
    void me_success() {

        // given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        when(memberProfileRepository.findWithAvatarByMemberId(memberId)).thenReturn(
            Optional.of(memberDetail));

        // when
        MemberDetail result = memberService.me(1L);

        // then
        assertThat(result.getId()).isEqualTo(memberProfileId);
        assertThat(result.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getUserhandle()).isEqualTo(userHandle);
        assertThat(result.getNickname()).isEqualTo(nickname);
    }

    @DisplayName("실패[me] - 존재하지 않는 회원 조회")
    @Test
    void me_fail_not_exists_member() {
        Long notExistsId = 9999L;

        ApiException exception = assertThrows(ApiException.class,
            () -> memberService.me(notExistsId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @DisplayName("실패[me] - 가입이 필요한 외부 회원")
    @Test
    void me_fail() {

        // given
        Long externalMemberId = 1000L;
        Member externalPendingMember = Member.builder()
            .id(externalMemberId)
            .loginType(LoginType.EXTERNAL)
            .status(MemberStatus.PENDING)
            .build();

        when(memberRepository.findById(externalMemberId)).thenReturn(Optional.of(externalPendingMember));

        // then
        ApiException exception = assertThrows(ApiException.class,
            () -> memberService.me(externalMemberId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NEED_SIGN_UP);

    }

    @DisplayName("실패[me] - 존재하지 않는 회원 프로필 조회")
    @Test
    void me_fail_not_exists_member_profile() {

        // given
        when(memberRepository.findById(9999L)).thenReturn(Optional.of(member));

        Long notExistsId = 9999L;

        // when
        InvalidStateException exception = assertThrows(InvalidStateException.class,
            () -> memberService.me(notExistsId));

        // then
        assertThat(exception.getMessage()).isEqualTo("[me] 회원 " + member.getId() + "의 프로필이 존재하지 않습니다");
    }


}