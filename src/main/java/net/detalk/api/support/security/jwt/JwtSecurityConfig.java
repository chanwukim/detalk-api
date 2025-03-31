package net.detalk.api.support.security.jwt;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.RequiredArgsConstructor;
import net.detalk.api.auth.service.JwtOAuth2UserService;
import net.detalk.api.support.filter.JwtAuthenticationFilter;
import net.detalk.api.support.filter.JwtExceptionFilter;
import net.detalk.api.support.security.oauth.OAuthFailHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtSecurityConfig {

    private final JwtOAuth2UserService jwtOAuth2UserService;
    private final JwtOAuth2SuccessHandler jwtOAuth2SuccessHandler;
    private final OAuthFailHandler oAuthFailHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final AuthenticationEntryPoint unauthorizedHandler;
    private final AccessDeniedHandler accessDeniedHandler;
    private final JwtLogoutHandler jwtLogoutHandler;

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS)
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v2/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/posts/filter/by-tags").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/posts/{id}/recommend").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/me/posts").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/members/me/profile").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/members/me/profile").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}/recommended-posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/{userhandle}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/images/upload-url").hasAnyRole("MEMBER","ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(jwtOAuth2UserService))
                .successHandler(jwtOAuth2SuccessHandler)
                .failureHandler(oAuthFailHandler)
            )
            .exceptionHandling(exceptionConfig -> exceptionConfig
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/v2/auth/sign-out")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
            )
            .addFilterBefore(jwtExceptionFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
