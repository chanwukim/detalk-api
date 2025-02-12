package net.detalk.api.support.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {
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
}
