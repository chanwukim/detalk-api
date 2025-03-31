package net.detalk.api.support.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.support.security.jwt.JwtConstants;
import net.detalk.api.support.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증 필터
 * 요청 헤더에서 JWT 토큰을 추출하고 검증하여 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConstants jwtConstants;
    private final static String ACCESS_TOKEN = "accessToken";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        String healthCheckPath = "/api/health";
        String refreshPath = jwtConstants.getRefreshPath() + "/refresh";

        if (refreshPath.equals(path) || healthCheckPath.equals(path)) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                jwtTokenProvider.validateAccessToken(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtTokenException e) {
                // 예외 처리는 JwtExceptionFilter 하므로 여기서는 무시
                // SecurityContext 명시적으로 정리
                SecurityContextHolder.clearContext();
                // JwtExceptionFilter 예외 전달
                throw e;
            }

        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        // 쿠키 자체가 빈 값
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN.equals(cookie.getName())) {
                String accessTokenValue = cookie.getValue();
                if (!accessTokenValue.isEmpty()) {
                    return accessTokenValue;
                }else {
                    return null;
                }
            }
        }
        return null;
    }

}
