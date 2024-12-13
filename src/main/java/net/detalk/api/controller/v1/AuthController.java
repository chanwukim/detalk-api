package net.detalk.api.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.AuthToken;
import net.detalk.api.service.AuthService;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.util.CookieUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final Environment env;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookie("rt", request)
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED))
            .getValue();

        AuthToken authToken = authService.refresh(refreshToken);

        boolean secure = !Arrays.asList(env.getActiveProfiles()).contains("dev");

        ResponseCookie accessTokenCookie = ResponseCookie
            .from("at", authToken.accessToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path("/")
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie
            .from("rt", authToken.refreshToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path("/")
            .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity
            .noContent()
            .build();
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookie("rt", request)
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED))
            .getValue();

        authService.signOut(refreshToken);

        CookieUtil.deleteCookie("at", request, response);
        CookieUtil.deleteCookie("rt", request, response);

        return ResponseEntity
            .noContent()
            .build();
    }
}
