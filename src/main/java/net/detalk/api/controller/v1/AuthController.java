package net.detalk.api.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.detalk.api.service.AuthService;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.util.CookieUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
