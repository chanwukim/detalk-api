package net.detalk.api.support.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.service.VisitorLogService;
import net.detalk.api.support.util.CookieUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 방문자의 위치 정보를 로깅하는 필터
 * - 요청당 한 번만 실행
 * - 수집된 정보를 비동기적으로 데이터베이스에 저장
 * - 세션 또는 쿠키를 통한 중복 로깅 방지
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GeoLoggingFilter extends OncePerRequestFilter {

    private final VisitorLogService visitorLogService;

    private static final String GEO_LOGGED_KEY = "geoLogged";
    private static final String NEXT_JS_SSR_AGENT = "node";

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain) throws ServletException, IOException {

        // 이미 로깅되었는지 확인
        if (!isGeoLogged(request)) {

            HttpSession session = request.getSession(false);

            // 세션 없고, 쿠키도 없으면 세션 생성. 첫 방문 경우에만 세션 생성
            if (session == null && !CookieUtil.getCookie(GEO_LOGGED_KEY, request).isPresent()) {
                session = request.getSession(true);
            }

            String userAgent = request.getHeader("User-Agent");

            // Next.js SSR 요청이면 위치정보 저장 안 함
            if (userAgent != null && userAgent.toLowerCase().contains(NEXT_JS_SSR_AGENT)) {
                if (log.isDebugEnabled()) {
                    log.debug("Next.js SSR 요청 (NODE) => 위치정보 저장 안함");
                }
            }else{
                String clientIp = getClientIp(request);
                String sessionId = (session != null) ? session.getId() : null;
                String referer = request.getHeader("Referer");
                log.debug("사용자 위치 정보 => IP: {}, Session: {}, UserAgent: {}, Referer: {}",
                    clientIp,
                    sessionId,
                    userAgent,
                    referer
                );

                // 위치 정보 DB 저장 (비동기로 처리)
                visitorLogService.saveVisitorLocation(
                    clientIp,
                    sessionId,
                    userAgent,
                    referer
                );
            }

            // 세션이나 쿠키에 로그 기록 표시
            markGeoLogged(request, response);

        }
        filterChain.doFilter(request, response);
    }

    /**
     * 방문자의 위치 정보가 이미 로깅되었는지 확인
     *
     * @param request HTTP 요청
     * @return 이미 로깅되었으면 true, 아니면 false
     */
    private boolean isGeoLogged(HttpServletRequest request) {

        if (request == null) {
            return false;
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            return session.getAttribute(GEO_LOGGED_KEY) != null;
        }

        // 세션이 없는데 쿠키가 있으면 이미 기록된 것으로 간주
        return CookieUtil.getCookie(GEO_LOGGED_KEY, request).isPresent();

    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private void markGeoLogged(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null) {
            session.setAttribute(GEO_LOGGED_KEY, Boolean.TRUE);
        } else {
            var cookie = new Cookie(GEO_LOGGED_KEY, "true");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);
        }
    }
}
