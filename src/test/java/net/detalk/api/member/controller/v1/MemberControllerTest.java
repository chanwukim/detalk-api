package net.detalk.api.member.controller.v1;

import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.member.domain.MemberStatus;
import net.detalk.api.member.domain.exception.MemberNeedSignUpException;
import net.detalk.api.member.domain.exception.MemberNotFoundException;
import net.detalk.api.member.domain.exception.UserHandleDuplicatedException;
import net.detalk.api.member.service.MemberService;
import net.detalk.api.post.controller.v1.response.GetProductPostResponse;
import net.detalk.api.post.controller.v1.response.GetProductPostResponse.Media;
import net.detalk.api.post.service.ProductPostService;
import net.detalk.api.support.BaseControllerTest;
import net.detalk.api.support.filter.GeoLoggingFilter;
import net.detalk.api.support.filter.MDCFilter;
import net.detalk.api.support.paging.CursorPageData;
import net.detalk.api.support.security.SecurityRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    @DisplayName("[실패] POST /api/v1/members/profile - 중복 userhandle로 프로필 생성")
    @Test
    void registerProfile_fail_duplicated_userHandle() throws Exception {
        // given
        var userHandle = "duplicated_userHandle";
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
        UserHandleDuplicatedException exception = new UserHandleDuplicatedException(userHandle);

        given(memberService.registerProfile(memberId, userHandle, nickname)).willThrow(exception);

        var expectedErrorJson =
            """
            {
                "code": "user_handle_conflict",
                "message": "이미 존재하는 userhandle입니다: duplicated_userHandle",
                "details": null
            }
            """;

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
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedErrorJson))
            .andDo(print());
    }

    @DisplayName("[성공] PUT /api/v1/members/profile - 프로필 수정")
    @Test
    void updateProfile_success() throws Exception {
        // given
        var memberId = 1L;
        var memberRole = SecurityRole.MEMBER;
        var newUserHandle = "updatedHandle";
        var newNickname = "업데이트닉네임";
        var newAvatarId = "updated-avatar-uuid";
        var newDescription = "수정된 자기소개입니다.";

        var requestJson = """
        {
            "userhandle": "%s",
            "avatarId": "%s",
            "nickname": "%s",
            "description": "%s"
        }
        """.formatted(newUserHandle, newAvatarId, newNickname, newDescription);

        Authentication testAuthentication = createTestAuthentication(memberId, memberRole);

        ResultActions resultActions = mockMvc.perform(
            put("/api/v1/members/profile")
                .with(authentication(testAuthentication))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions
            .andExpect(status().isNoContent())
            .andDo(print());

    }

    @DisplayName("[성공] GET /api/v1/members/me/posts - 내 게시글 목록 조회")
    @Test
    void getMyPosts_success_firstPage() throws Exception {
        // given
        var memberId = 1L;
        var memberRole = SecurityRole.MEMBER;
        var pageSize = 5;

        var testAuthentication = createTestAuthentication(memberId, memberRole);

        var mediaList = List.of(new GetProductPostResponse.Media("http://image.url/1.jpg", 1));

        var firstPost = GetProductPostResponse.builder()
            .id(10L)
            .nickname("user1")
            .title("첫번째 글")
            .createdAt(Instant.parse("2025-04-04T10:00:00Z"))
            .media(mediaList)
            .build();

        var secondPost = GetProductPostResponse.builder()
            .id(9L)
            .nickname("user1")
            .title("두번째 글")
            .createdAt(Instant.parse("2025-04-04T09:59:00Z"))
            .media(mediaList)
            .build();

        var postList = List.of(firstPost, secondPost);

        var expectedNextId = 9L;
        boolean hasNext = true;

        var expectedResponse = new CursorPageData<>(postList, expectedNextId, hasNext);

        given(productPostService.getProductPostsByMemberId(memberId, pageSize, null))
            .willReturn(expectedResponse);

        String expectedResponseJson = objectMapper.writeValueAsString(expectedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/me/posts")
                .param("size", String.valueOf(pageSize))
                .with(authentication(testAuthentication))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponseJson))
            .andDo(print());
    }

    @DisplayName("[성공] GET /api/v1/members/{userhandle}/posts - 특정 사용자 게시글 목록 조회")
    @Test
    void getPostsByUserHandle_success_firstPage() throws Exception {
        // given
        var userhandle = "testuser";
        var memberId = 100L;
        var pageSize = 3;
        SecurityRole memberRole = SecurityRole.MEMBER;
        var testAuthentication = createTestAuthentication(memberId, memberRole);

        given(memberService.getMemberIdByUserHandle(userhandle)).willReturn(memberId);

        var mediaList = List.of(new GetProductPostResponse.Media("http://image.url/1.jpg", 1));

        var firstPost = GetProductPostResponse.builder()
            .id(50L)
            .nickname("user1")
            .title("첫번째 글")
            .createdAt(Instant.parse("2025-04-04T10:00:00Z"))
            .media(mediaList)
            .build();

        var secondPost = GetProductPostResponse.builder()
            .id(45L)
            .nickname("user1")
            .title("두번째 글")
            .createdAt(Instant.parse("2025-04-04T09:59:00Z"))
            .media(mediaList)
            .build();

        var postList = List.of(firstPost, secondPost);

        var expectedNextId = 45L;
        var hasNext = false; // 마지막 페이지라고 가정

        var expectedServiceResponse = new CursorPageData<>(postList, expectedNextId, hasNext);

        given(productPostService.getProductPostsByMemberId(memberId, pageSize, null))
            .willReturn(expectedServiceResponse);

        String expectedResponseJson = objectMapper.writeValueAsString(expectedServiceResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/{userhandle}/posts", userhandle)
                .param("size", String.valueOf(pageSize))
                .with(authentication(testAuthentication))
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponseJson))
            .andDo(print());
    }

    @DisplayName("[성공] GET /api/v1/members/{userhandle} - 특정 사용자 프로필 조회")
    @Test
    void getMemberProfile_success() throws Exception {
        // given
        var userhandle = "targetUser";
        var expectedMemberId = 123L;
        var expectedNickname = "조회된닉네임";
        var expectedAvatarUrl = "http://avatar.url/target.png";
        var expectedDescription = "조회된 사용자의 자기소개";
        var memberId = 1L;
        var memberRole = SecurityRole.MEMBER;
        var testAuthentication = createTestAuthentication(memberId, memberRole);

        var expectedServiceResponse = GetMemberProfileResponse.builder()
            .id(expectedMemberId)
            .userhandle(userhandle)
            .nickname(expectedNickname)
            .avatarUrl(expectedAvatarUrl)
            .description(expectedDescription)
            .build();

        given(memberService.getMemberDetailByUserhandle(userhandle)).willReturn(
            expectedServiceResponse);

        var expectedResponseJson = """
        {
            "id": %d,
            "userhandle": "%s",
            "nickname": "%s",
            "avatarUrl": "%s",
            "description": "%s"
        }
        """.formatted(
            expectedServiceResponse.id(),
            expectedServiceResponse.userhandle(),
            expectedServiceResponse.nickname(),
            expectedServiceResponse.avatarUrl(),
            expectedServiceResponse.description()
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/members/{userhandle}", userhandle)
                .with(authentication(testAuthentication))
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
