package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.detalk.api.service.AuthService;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthFailHandler oAuthFailHandler;
    private final OAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final JwtOAuthSuccessHandler oAuthSuccessHandler;
    // SESSION
    //private final SessionAuthService authService;
    //private final SessionOAuthSuccessHandler oAuthSuccessHandler;
    //private final SessionLogoutSuccessHandler logoutSuccessHandler;

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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me/posts").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/recommended-posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tags").permitAll()
                .anyRequest().authenticated())
            /**
             * sign-in: /oauth2/authorization/{registrationId}
             * redirect: /login/oauth2/code/{registrationId}
             */
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorizationEndpointConfig ->
                    authorizationEndpointConfig.authorizationRequestRepository(authorizationRequestRepository))
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(
                    authService))
                .successHandler(oAuthSuccessHandler)
                .failureHandler(oAuthFailHandler))
            //.logout(logout -> logout
            //  .logoutUrl("/api/v1/auth/sign-out")
            //  .logoutSuccessHandler(logoutSuccessHandler)
            //)
            .addFilterBefore(new TokenFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(config -> config
                .authenticationEntryPoint(unauthorizedHandler())
                .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }
}
