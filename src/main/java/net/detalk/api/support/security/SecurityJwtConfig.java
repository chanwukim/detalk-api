package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.detalk.api.auth.service.JwtOAuth2Service;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import net.detalk.api.support.security.oauth.JwtOAuthSuccessHandler;
import net.detalk.api.support.security.oauth.OAuth2AuthorizationRequestRepository;
import net.detalk.api.support.security.oauth.OAuthFailHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

/**
 * @deprecated 백업w 코드
 */
@RequiredArgsConstructor
public class SecurityJwtConfig {
    private final OAuthFailHandler oAuthFailHandler;
    private final OAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final JwtOAuth2Service authService;
    private final TokenProvider tokenProvider;
    private final JwtOAuthSuccessHandler oAuthSuccessHandler;

    @Bean
    public TokenFilter tokenFilter() {
        return new TokenFilter(tokenProvider);
    }

    @Bean
    protected AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(new ErrorMessage(ErrorCode.UNAUTHORIZED)));
            writer.flush();
        };
    }

    @Bean
    protected AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(new ErrorMessage(ErrorCode.FORBIDDEN)));
            writer.flush();
        };
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http, TokenProvider tokenProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorizationEndpointConfig ->
                    authorizationEndpointConfig.authorizationRequestRepository(authorizationRequestRepository))
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(
                    authService))
                .successHandler(oAuthSuccessHandler)
                .failureHandler(oAuthFailHandler))
            .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(config -> config
                .authenticationEntryPoint(unauthorizedHandler())
                .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }
}
