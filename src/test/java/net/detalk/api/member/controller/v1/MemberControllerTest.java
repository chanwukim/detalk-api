package net.detalk.api.member.controller.v1;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.member.service.MemberService;
import net.detalk.api.post.service.ProductPostService;
import net.detalk.api.support.BaseControllerTest;
import net.detalk.api.support.config.WebConfig;
import net.detalk.api.support.filter.GeoLoggingFilter;
import net.detalk.api.support.filter.MDCFilter;
import net.detalk.api.support.security.SecurityRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
@Import(WebConfig.class)
class MemberControllerTest extends BaseControllerTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProductPostService productPostService;

    // MemberController 가 의존하는 클래스 Mock Bean 등록
    @TestConfiguration
    static class ControllerTestConfig {
        @Bean
        public MemberService memberService() { return mock(MemberService.class); }
        @Bean
        public ProductPostService productPostService() { return mock(ProductPostService.class); }
    }

    @DisplayName("GET /api/v1/members/me")
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

}