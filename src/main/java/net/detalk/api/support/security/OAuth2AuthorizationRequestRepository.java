package net.detalk.api.support.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import net.detalk.api.support.util.CookieUtil;
import net.detalk.api.support.util.StringUtil;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * OAuth2 인증 요청을 관리하는 레포지토리
 * 클라이언트의 redirect uri를 쿠키로 관리
 * 예: /oauth2/authorization/${provider}?redirect_uri=${redirectUri}
 *
 * OAuth2 인증 과정:
 * 1. 클라이언트가 소셜 로그인 요청 시 redirect_uri 파라미터 전달
 * 2. 해당 URI를 쿠키에 저장하고 OAuth2 인증 진행
 * 3. 인증 완료 후 저장된 redirect_uri로 리다이렉트
 *
 * 주요 기능:
 * - OAuth2 인증 요청 데이터는 세션에 저장 (HttpSessionOAuth2AuthorizationRequestRepository 활용)
 * - 클라이언트의 redirect_uri는 쿠키에 저장 (COOKIE_EXPIRE_SECONDS 동안 유지)
 * - 인증 완료 또는 실패 시 관련 데이터 정리 (세션 데이터 및 쿠키 삭제)
 */
@Component
public class OAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String REDIRECT_URI_COOKIE_NAME = "oauth2-redirect-uri";
    // 클라이언트가 보낸 oauth 처리후 redirect uri
    private static final String REDIRECT_URI_PARAM = "redirect_to";
    private static final int COOKIE_EXPIRE_SECONDS = 30; // 30초

    // 스프링 시큐리티 기본 세션 기반 OAuth2 인증 요청 저장소
    private final HttpSessionOAuth2AuthorizationRequestRepository sessionOAuth2AuthorizationRequestRepository =
        new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response) {
        sessionOAuth2AuthorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM);

        if (StringUtil.isNotEmpty(redirectUriAfterLogin)) {
            Cookie cookie = new Cookie(REDIRECT_URI_COOKIE_NAME, redirectUriAfterLogin);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
            response.addCookie(cookie);
        }
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return sessionOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response) {
        CookieUtil.deleteCookie(REDIRECT_URI_COOKIE_NAME, request, response);
        return sessionOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
    }
}
