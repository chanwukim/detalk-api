package net.detalk.api.support.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.service.RefreshTokenService;
import net.detalk.api.support.util.CookieUtil;
import net.detalk.api.support.util.EnvironmentHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtLogoutHandler implements LogoutHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private final RefreshTokenService refreshTokenService;
    private final JwtConstants jwtConstants;
    private final EnvironmentHolder env;


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {

        Optional<Cookie> refreshTokenCookie = CookieUtil.getCookie(REFRESH_TOKEN_COOKIE_NAME,
            request);

        refreshTokenCookie.ifPresent(cookie -> {
            String refreshToken = cookie.getValue();
            refreshTokenService.revokeRefreshToken(refreshToken);
        });

        boolean secure = "prod".equals(env.getActiveProfile());

        CookieUtil.deleteCookieWithPath(
            response,
            ACCESS_TOKEN_COOKIE_NAME,
            jwtConstants.getAccessPath(),
            secure
        );

        CookieUtil.deleteCookieWithPath(
            response,
            REFRESH_TOKEN_COOKIE_NAME,
            jwtConstants.getRefreshPath(),
            secure
        );


    }
}
