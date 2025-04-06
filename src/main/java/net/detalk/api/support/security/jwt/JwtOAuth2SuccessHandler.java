package net.detalk.api.support.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.service.RefreshTokenService;
import net.detalk.api.support.config.AppProperties;
import net.detalk.api.support.security.oauth.CustomOAuth2User;
import net.detalk.api.support.util.EnvironmentHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtConstants jwtConstants;
    private final AppProperties appProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final EnvironmentHolder env;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String authorities = oAuth2User.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        Long memberId = oAuth2User.getId();

        String accessToken = jwtTokenProvider.generateAccessToken(memberId, authorities);
        String refreshToken = refreshTokenService.createRefreshToken(memberId);

        boolean secure = "prod".equals(env.getActiveProfile());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .maxAge(jwtConstants.getRefreshTokenValidity())
            .path(jwtConstants.getRefreshPath())
            .secure(secure)
            .sameSite("Lax")
            .httpOnly(true)
            .build();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
            .maxAge(Integer.MAX_VALUE)
            .path(jwtConstants.getAccessPath())
            .secure(secure)
            .sameSite("Lax")
            .httpOnly(true)
            .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());
        response.addHeader("Set-Cookie", accessCookie.toString());

        String url = appProperties.getBaseUrl();

        var targetUrl = UriComponentsBuilder.fromUriString(url)
            .path("/sign-in/callback")
            .queryParam("auth", "success")
            .queryParam("token", accessToken)
            .build()
            .toUriString();

        redirectStrategy.sendRedirect(request, response, targetUrl);

    }

}
