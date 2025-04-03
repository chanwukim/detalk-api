package net.detalk.api.member.controller.v1;

import static org.mockito.BDDMockito.given;

import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.member.domain.MemberStatus;
import net.detalk.api.member.domain.exception.MemberNeedSignUpException;
import net.detalk.api.member.domain.exception.MemberNotFoundException;
import net.detalk.api.member.service.MemberService;
import net.detalk.api.post.service.ProductPostService;
import net.detalk.api.support.BaseControllerTest;
import net.detalk.api.support.filter.GeoLoggingFilter;
import net.detalk.api.support.filter.MDCFilter;
import net.detalk.api.support.security.SecurityRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = MemberController.class,
    excludeFilters = {
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GeoLoggingFilter.class),
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MDCFilter.class)
    }
)
class MemberControllerTest extends BaseControllerTest {

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private ProductPostService productPostService;

    @DisplayName("[성공] GET /api/v1/members/me - 회원 내 정보 조회")
    @Test
    void me() throws Exception {

        // given
        Long memberId = 1L;
        SecurityRole memberRole = SecurityRole.MEMBER;
        String expectedNickname = "테스트닉네임";
        String expectedUserHandle = "test_handle";
        String expectedAvatarUrl = "http://avatar.url/test.jpg";
        String expectedDescription = "자기소개입니다.";

        GetMemberProfileResponse expectedResponse = GetMemberProfileResponse.builder()
            .id(memberId)
            .nickname(expectedNickname)
            .userhandle(expectedUserHandle)
            .avatarUrl(expectedAvatarUrl)
            .description(expectedDescription)
            .build();

        given(memberService.me(memberId)).willReturn(expectedResponse);

        String expectedJson = """
        {
            "id": %d,
            "nickname": "%s",
            "userhandle": "%s",
            "avatarUrl": "%s",
            "description": "%s"
        }
        """.formatted(memberId, expectedNickname, expectedUserHandle, expectedAvatarUrl, expectedDescription);

        Authentication testAuthentication = createTestAuthentication(memberId, memberRole);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/me")
                .with(authentication(testAuthentication))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))
            .andDo(print());
    }

    @DisplayName("[실패] GET /api/v1/members/me - 존재하지 않는 회원 ID")
    @Test
    void me_fail_memberNotFound_withTextBlock() throws Exception {
        // given
        Long memberId = 999L;
        SecurityRole memberRole = SecurityRole.MEMBER;
        Authentication testAuthentication = createTestAuthentication(memberId, memberRole);

        // MemberNotFoundException 발생 가정
        MemberNotFoundException exception = new MemberNotFoundException();
        given(memberService.me(memberId)).willThrow(exception);

        var expectedErrorJson =
        """
            {
              "code":"member_not_found",
              "message":"해당 회원을 찾을 수 없습니다.",
              "details":null
            }
        """;

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/me")
                .with(authentication(testAuthentication))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedErrorJson))
            .andDo(print());

    }

    @DisplayName("[실패] GET /api/v1/members/me - 가입 대기 상태 회원")
    @Test
    void me_fail_pendingMember_withTextBlock() throws Exception {
        // given
        Long pendingMemberId = 2L;
        SecurityRole memberRole = SecurityRole.MEMBER;
        Authentication testAuthentication = createTestAuthentication(pendingMemberId, memberRole);
        MemberStatus pendingStatus = MemberStatus.PENDING;

        MemberNeedSignUpException exception = new MemberNeedSignUpException(pendingStatus);
        given(memberService.me(pendingMemberId)).willThrow(exception);

        var expectedErrorJson =
        """
        {
            "code": "need_sign_up",
            "message": "회원가입이 필요한 외부 회원입니다. memberStatus=PENDING",
            "details": null
        }
        """;

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/me")
                .with(authentication(testAuthentication))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedErrorJson))
            .andDo(print());
    }

    @DisplayName("[실패] GET /api/v1/members/me - 잘못된 Principal 타입")
    @Test
    void me_fail_wrongPrincipalType_withTextBlock() throws Exception {
        // given
        Authentication wrongPrincipalAuth = new TestingAuthenticationToken("user-principal-string", null, "ROLE_MEMBER");

        // 예상되는 에러 JSON 응답 (SessionUserNotFoundException 메시지 기반, 여기만 var 사용)
        var expectedErrorJson = """
        {
            "code": "session_user_not_found",
            "message": "User session not found. Please log in",
            "details" : null
        }
        """;

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/me")
                .with(authentication(wrongPrincipalAuth))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedErrorJson))
            .andDo(print());

    }

    @DisplayName("[성공] POST /api/v1/members/profile - 프로필 생성")
    @Test
    void registerProfile() throws Exception {

        // given
        var userHandle = "testHandle";
        var nickname = "testNickname";
        var memberId = 1L;
        var memberRole = SecurityRole.MEMBER;

        var requestJson = """
            {
                "userhandle": "%s",
                "nickname": "%s"
            }
            """.formatted(userHandle, nickname);

        Authentication testAuthentication = createTestAuthentication(memberId, memberRole);

        GetMemberProfileResponse expectedResult = GetMemberProfileResponse.builder()
            .id(memberId)
            .userhandle(userHandle)
            .nickname(nickname)
            .avatarUrl("default_avatar.jpg")
            .description("자기소개가 없습니다.")
            .build();

        given(memberService.registerProfile(memberId, userHandle, nickname)).willReturn(
            expectedResult);

        var expectedResponseJson = """
        {
            "id": %d,
            "userhandle": "%s",
            "nickname": "%s",
            "avatarUrl": "%s",
            "description": "%s"
        }
        """.formatted(
            expectedResult.id(),
            expectedResult.userhandle(),
            expectedResult.nickname(),
            expectedResult.avatarUrl(),
            expectedResult.description()
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            post("/api/v1/members/profile")
                .with(authentication(testAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponseJson))
            .andDo(print());
    }
}
