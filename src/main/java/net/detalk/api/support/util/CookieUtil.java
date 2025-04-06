package net.detalk.api.support.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private CookieUtil() {}

    public static Optional<Cookie> getCookie(String name, HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst();
    }

    public static void deleteCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        getCookie(name, request).ifPresent(cookie -> {
            Cookie deleteCookie = new Cookie(cookie.getName(), null);
            deleteCookie.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
            deleteCookie.setMaxAge(0);
            deleteCookie.setSecure(cookie.getSecure());
            deleteCookie.setHttpOnly(cookie.isHttpOnly());

            if (cookie.getDomain() != null) {
                deleteCookie.setDomain(cookie.getDomain());
            }

            response.addCookie(deleteCookie);
        });
    }

    /**
     * 클라이언트에게 쿠키를 삭제하도록 응답 헤더를 설정합니다. (ResponseCookie 사용)
     *
     * @param response   HttpServletResponse
     * @param cookieName 삭제할 쿠키 이름
     * @param path       쿠키 경로 (생성 시와 동일해야 함)
     */
    public static void deleteCookieWithPath(HttpServletResponse response, String cookieName,
        String path, boolean isSecure) {

        ResponseCookie deleteCookie = ResponseCookie.from(cookieName, "")
            .maxAge(0)
            .path(path)
            .secure(isSecure)
            .sameSite("Lax")
            .httpOnly(true)
            .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }
}
