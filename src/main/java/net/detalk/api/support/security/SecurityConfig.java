package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.detalk.api.service.SessionAuthService;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import net.detalk.api.support.filter.MDCFilter;
import net.detalk.api.support.security.oauth.OAuthFailHandler;
import net.detalk.api.support.security.oauth.SessionOAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthFailHandler oAuthFailHandler;
    private final MDCFilter mdcFilter;
    private final SessionAuthService authService;
    private final SessionOAuthSuccessHandler oAuthSuccessHandler;
    private final SessionLogoutSuccessHandler logoutSuccessHandler;

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

    // 실시간 세션 조회
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // 세션 생명주기 이벤트 캡처
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
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
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(3)
                .sessionRegistry(sessionRegistry())
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts/filter/by-tags").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/posts/{id}/recommend").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me/posts").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/members/me/profile").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/v1/members/me/profile").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/recommended-posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tags").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/images/upload-url").hasRole("MEMBER")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            /**
             * sign-in: /oauth2/authorization/{registrationId}
             * redirect: /login/oauth2/code/{registrationId}
             */
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfoEndpointConfig ->
                    userInfoEndpointConfig.userService(authService))
                .successHandler(oAuthSuccessHandler)
                .failureHandler(oAuthFailHandler))
            .logout(logout -> logout
              .logoutUrl("/api/v1/auth/sign-out")
              .logoutSuccessHandler(logoutSuccessHandler)
            )
            .addFilterBefore(mdcFilter,UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(config -> config
                .authenticationEntryPoint(unauthorizedHandler())
                .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }
}
