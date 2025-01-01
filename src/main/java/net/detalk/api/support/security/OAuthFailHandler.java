package net.detalk.api.support.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.AppProperties;
import net.detalk.api.support.util.CookieUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static net.detalk.api.support.security.OAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuthFailHandler implements AuthenticationFailureHandler {
    private final AppProperties appProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws ServletException, IOException {

        String redirectUrl = CookieUtil.getCookie(REDIRECT_URI_COOKIE_NAME, request)
            .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8))
            .orElse(appProperties.getBaseUrl());

        redirectStrategy.sendRedirect(request, response, redirectUrl + "?ok=false");
    }
}
