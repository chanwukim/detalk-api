package net.detalk.api.auth.controller.v2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.security.jwt.JwtConstants;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.auth.controller.v2.response.JwtTokenResponse;
import net.detalk.api.auth.service.RefreshTokenService;
import net.detalk.api.support.util.EnvironmentHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtAuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtConstants jwtConstants;
    private final EnvironmentHolder env;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request,
        HttpServletResponse response) {

        Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");

        if (refreshTokenCookie == null) {
            log.debug("Refresh token cookie not found in request");
            throw new JwtTokenException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String refreshToken = refreshTokenCookie.getValue();

        JwtTokenResponse tokenResponse = refreshTokenService.refreshAccessToken(
            refreshToken
        );

        boolean secure = "prod".equals(env.getActiveProfile());

        ResponseCookie newRefreshTokenCookie = ResponseCookie
            .from("refreshToken", tokenResponse.refreshToken())
            .maxAge(jwtConstants.getRefreshTokenValidity())
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path(jwtConstants.getRefreshPath())
            .build();

        response.addHeader("Set-Cookie", newRefreshTokenCookie.toString());

        Map<String, String> tokenBearerResponse = new HashMap<>();
        tokenBearerResponse.put("accessToken", tokenResponse.accessToken());

        return ResponseEntity.ok(tokenBearerResponse);
    }

}
