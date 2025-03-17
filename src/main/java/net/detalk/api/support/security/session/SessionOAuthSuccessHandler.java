package net.detalk.api.support.security.session;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.config.AppProperties;
import net.detalk.api.support.security.SecurityUser;
import net.detalk.api.support.security.oauth.CustomOAuth2User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "session")
public class SessionOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final AppProperties appProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        // oauth2 -> security user
        if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {

            SecurityUser securityUser = new SecurityUser(customOAuth2User.getId(),
                customOAuth2User.getAuthorities());

            UsernamePasswordAuthenticationToken securityAuthentication =
                new UsernamePasswordAuthenticationToken(securityUser, null,
                    securityUser.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(securityAuthentication);
        } else {
            log.error("[onAuthenticationSuccess] 시큐리티 컨텍스트 홀더 등록 실패");
            String url = appProperties.getBaseUrl();
            redirectStrategy.sendRedirect(request, response, url);
            return;
        }
        String url = appProperties.getBaseUrl();
        redirectStrategy.sendRedirect(request, response, url);
    }
}
