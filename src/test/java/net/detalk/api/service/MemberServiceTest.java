package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.detalk.api.controller.v1.request.UpdateProfileRequest;
import net.detalk.api.domain.LoginType;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberProfile;
import net.detalk.api.domain.MemberStatus;
import net.detalk.api.domain.exception.MemberNotFoundException;
import net.detalk.api.domain.exception.MemberProfileNotFoundException;
import net.detalk.api.domain.exception.UserHandleDuplicatedException;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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
    private TimeHolder timeHolder;
    private UUIDGenerator uuidGenerator;

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
        timeHolder = new FakeTimeHolder(Instant.parse("2025-01-01T12:00:00Z"));
        uuidGenerator = new FakeUUIDGenerator(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        memberService = new MemberService(
            memberRepository,
            memberProfileRepository,
            timeHolder,
            uuidGenerator
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

        assertThat(exception.getErrorCode()).isEqualTo("member_not_found");
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

        when(memberRepository.findById(externalMemberId)).thenReturn(
            Optional.of(externalPendingMember));

        // then
        ApiException exception = assertThrows(ApiException.class,
            () -> memberService.me(externalMemberId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo("need_sign_up");

    }

    @DisplayName("실패[me] - 존재하지 않는 회원 프로필 조회")
    @Test
    void me_fail_not_exists_member_profile() {

        // given
        when(memberRepository.findById(9999L)).thenReturn(Optional.of(member));

        Long notExistsId = 9999L;

        // when
        MemberProfileNotFoundException exception = assertThrows(MemberProfileNotFoundException.class,
            () -> memberService.me(notExistsId));

        // then
        assertThat(exception.getMessage()).isEqualTo(
            "회원 프로필을 찾을 수 없습니다.");
    }

    @DisplayName("성공[registerProfile]")
    @Test
    void registerProfile_success() {

        // given
        Member pendingExternalMember = Member.builder()
            .id(memberId)
            .loginType(LoginType.EXTERNAL)
            .status(MemberStatus.PENDING)
            .createdAt(timeHolder.now())
            .updatedAt(timeHolder.now())
            .build();
        when(memberProfileRepository.findByUserHandle(userHandle)).thenReturn(Optional.empty());
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(pendingExternalMember));

        // 가입 후 Profile 찾기
        MemberProfile existingProfile = MemberProfile.builder()
            .id(memberProfileId)
            .memberId(memberId)
            .avatarId(uuidGenerator.generateV7())
            .updatedAt(null)
            .build();
        when(memberProfileRepository.findByMemberId(memberId)).thenReturn(
            Optional.of(existingProfile));

        // Profile 업데이트 후 반환
        MemberProfile updatedProfile = MemberProfile.builder()
            .id(memberProfileId)
            .memberId(memberId)
            .userhandle(userHandle)
            .nickname(nickname)
            .avatarId(existingProfile.getAvatarId())
            .updatedAt(timeHolder.now())
            .build();
        when(memberProfileRepository.update(any(MemberProfile.class))).thenReturn(updatedProfile);

        // when
        MemberDetail result = memberService.registerProfile(memberId, userHandle, nickname);

        // then
        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getUserhandle()).isEqualTo(userHandle);
        assertThat(result.getNickname()).isEqualTo(nickname);
    }

    @DisplayName("실패[registerProfile] - userHandle 중복")
    @Test
    void registerProfile_fail_conflictUserHandle() {
        // given
        when(memberProfileRepository.findByUserHandle(userHandle))
            .thenReturn(Optional.of(MemberProfile.builder().build()));

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> memberService.registerProfile(memberId, userHandle, nickname));

        // then
        assertThat(exception.getErrorCode()).isEqualTo("user_handle_conflict");
    }

    @DisplayName("실패[registerProfile] - 존재하지 않는 회원")
    @Test
    void registerProfile_fail_notExistsMember() {
        // given
        Long notExistsId = 9999L;
        when(memberProfileRepository.findByUserHandle(userHandle)).thenReturn(Optional.empty());
        when(memberRepository.findById(notExistsId)).thenReturn(Optional.empty());

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> memberService.registerProfile(notExistsId, userHandle, nickname));

        // then
        assertThat(exception.getErrorCode()).isEqualTo("member_not_found");
    }


    @DisplayName("실패[registerProfile] - 프로필이 존재하지 않음")
    @Test
    void registerProfile_fail_notExistsMemberProfile() {
        // given
        Member pendingMember = Member.builder()
            .id(memberId)
            .loginType(LoginType.EXTERNAL)
            .status(MemberStatus.PENDING)
            .build();

        when(memberProfileRepository.findByUserHandle(userHandle)).thenReturn(Optional.empty());
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(pendingMember));

        // 프로필 조회 실패
        when(memberProfileRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when
        MemberProfileNotFoundException exception = assertThrows(MemberProfileNotFoundException.class,
            () -> memberService.registerProfile(memberId, userHandle, nickname));

        // then
        assertThat(exception.getMessage())
            .isEqualTo("회원 프로필을 찾을 수 없습니다.");
    }

    @DisplayName("성공[updateProfile] 신규 userhandle 및 avatarId 제공 시, 정상 업데이트")
    @Test
    void updateProfile_WhenNewUserHandleIsDifferentAndAvatarProvided_ShouldUpdateProfile() {

        var oldUserHandle = "oldUserHandle";
        var oldNickname = "oldNickname";
        var oldDescription = "oldDescription";

        // given
        MemberProfile oldProfile = MemberProfile.builder()
            .id(1L)
            .memberId(memberId)
            .avatarId(uuidGenerator.generateV7())
            .userhandle(oldUserHandle)
            .nickname(oldNickname)
            .description(oldDescription)
            .updatedAt(timeHolder.now())
            .build();

        UUIDGenerator newUUIDgenerator = new FakeUUIDGenerator(
            UUID.fromString("999999-e89b-12d3-a456-426614174000"));

        UUID newAvatarId = newUUIDgenerator.generateV7();

        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
            .userandle("newUserHandle")
            .avatarId(String.valueOf(newAvatarId))
            .nickname("newNickname")
            .description("newDescription")
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberProfileRepository.findByMemberId(memberId)).thenReturn(Optional.of(oldProfile));

        // when
        memberService.updateProfile(memberId, updateRequest);
    }

    @DisplayName("실패[updateProfile] 존재하지 않는 MemberId 요청")
    @Test
    void updateProfile_WhenMemberIdNotFound() {

        // given
        var notExistsMemberId = 9999L;
        var updateRequest = UpdateProfileRequest.builder()
            .userandle("newUserHandle")
            .avatarId("asdfasdf")
            .nickname("newNickname")
            .description("newDescription")
            .build();

        // when
        MemberNotFoundException exception = assertThrows(
            MemberNotFoundException.class,
            () -> memberService.updateProfile(notExistsMemberId, updateRequest));

        // then
        assertThat(exception.getMessage()).isEqualTo("해당 회원을 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo("member_not_found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("실패[updateProfile] MemberId에 해당하는 프로파일이 존재하지 않음")
    @Test
    void updateProfile_WhenNotExistsMemberProfileWithMemberId() {

        // given
        var memberId = 9999L;
        var updateRequest = UpdateProfileRequest.builder()
            .userandle("newUserHandle")
            .avatarId("asdfasdf")
            .nickname("newNickname")
            .description("newDescription")
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.ofNullable(member));

        // when
        MemberProfileNotFoundException exception = assertThrows(
            MemberProfileNotFoundException.class,
            () -> memberService.updateProfile(memberId, updateRequest));

        assertThat(exception.getMessage()).isEqualTo("회원 프로필을 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo("member_profile_not_found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("성공 [findProfileByUserhandle] 회원 프로필 조회")
    @Test
    void findProfileByUserhandle() {

        // given
        MemberProfile memberProfile = MemberProfile.builder()
            .id(1L)
            .memberId(memberId)
            .avatarId(uuidGenerator.generateV7())
            .userhandle("userHandle")
            .nickname("hello")
            .description("desc")
            .updatedAt(timeHolder.now())
            .build();

        var userHandle = "userHandle";
        when(memberProfileRepository.findByUserHandle(userHandle)).thenReturn(
            Optional.ofNullable(memberProfile));

        // when
        MemberProfile result = memberService.findProfileByUserhandle(userHandle);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getAvatarId()).isEqualTo(uuidGenerator.generateV7());
        assertThat(result.getUserhandle()).isEqualTo("userHandle");
        assertThat(result.getNickname()).isEqualTo("hello");
        assertThat(result.getDescription()).isEqualTo("desc");
    }

    @DisplayName("성공 [findProfileByUserhandle] 회원 프로필 조회")
    @Test
    void findProfileByUserhandleFailWhenMemberProfileNotExistsWithUserHandle() {

        var userHandle = "notExistsHandle";

        MemberProfileNotFoundException exception = assertThrows(
            MemberProfileNotFoundException.class,
            () -> memberService.findProfileByUserhandle(userHandle));

        assertThat(exception.getMessage()).isEqualTo("(userhandle: notExistsHandle)에 해당하는 회원 프로필을 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo("member_profile_not_found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("성공[findMemberIdByUserHandle]UserHandle로 MemberId 조회")
    @Test
    void findMemberIdByUserHandle() {
        // given
        MemberProfile memberProfile = MemberProfile.builder()
            .id(1L)
            .memberId(memberId)
            .avatarId(uuidGenerator.generateV7())
            .userhandle("userHandle")
            .nickname("hello")
            .description("desc")
            .updatedAt(timeHolder.now())
            .build();

        var userHandle = "userHandle";
        when(memberProfileRepository.findByUserHandle(userHandle)).thenReturn(
            Optional.ofNullable(memberProfile));

        // when
        Long foundMemberId = memberService.findMemberIdByUserHandle(userHandle);

        // then
        assertThat(foundMemberId).isEqualTo(memberId);
    }

    @Test
    void checkDuplicateUserHandle() {
        var userHandle = "userHandle";

        when(memberProfileRepository.existsByUserHandle(userHandle)).thenReturn(true);

        UserHandleDuplicatedException exception = assertThrows(
            UserHandleDuplicatedException.class,
            () -> memberService.checkDuplicateUserHandle(userHandle));

        assertThat(exception.getMessage()).isEqualTo(String.format("이미 존재하는 userhandle입니다: %s", userHandle));
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getErrorCode()).isEqualTo("user_handle_conflict");
    }


}