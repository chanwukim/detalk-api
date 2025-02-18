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

@Component
@Slf4j
@RequiredArgsConstructor
public class GeoLoggingFilter extends OncePerRequestFilter {

    private final VisitorLogService visitorLogService;

    private static final String GEO_LOGGED_KEY = "geoLogged";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(true);

        // 로그가 저장되지 않을 경우 새로 저장한다
        if (!isGeoLogged(request)) {

            String userAgent = request.getHeader("User-Agent");

            if (userAgent != null && userAgent.toLowerCase().contains("node")) {
                log.debug("Next.js SSR 요청 (NODE) => 위치정보 저장 안함");
            }else{
                String clientIp = getClientIp(request);
                String sessionId = session.getId();
                String referer = request.getHeader("Referer");
                log.debug("사용자 위치 정보 => IP: {}, Session: {}, UserAgent: {}, Referer: {}",
                    clientIp,
                    sessionId,
                    userAgent,
                    referer
                );

                // 위치 정보 DB 저장
                visitorLogService.saveVisitorLocation(
                    clientIp,
                    sessionId,
                    userAgent,
                    referer
                );
            }

            // 세션에 로그 저장했다고 설정
            markGeoLogged(request, response);

        }
        filterChain.doFilter(request, response);
    }

    private boolean isGeoLogged(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            // 세션 존재하고, GEO_LOGGED_KEY == true 면 이미 기록한 위치정보
            return session.getAttribute(GEO_LOGGED_KEY) != null;
        }     // 세션 없는데 GEO_LOGGED_KEY 쿠키가 존재하면 이미 기록한 위치 정보
        else {
            return CookieUtil.getCookie(GEO_LOGGED_KEY, request).isPresent();
        }
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
