package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;


/**
 * 이 클래스는 Spring Security에서 인증 예외를 처리하기 위한 핸들러 빈을 정의합니다.
 * <p>
 * 두 가지 주요 예외 상황을 처리합니다:
 * <ul>
 *   <li>인증되지 않은 요청에 대한 응답 처리 (HTTP 401 Unauthorized)</li>
 *   <li>권한이 없는 요청에 대한 응답 처리 (HTTP 403 Forbidden)</li>
 * </ul>
 * </p>
 * 각각의 예외 상황에 대해 JSON 형식의 에러 메시지를 응답으로 반환합니다.
 */
@RequiredArgsConstructor
@Configuration
public class SecurityExceptionHandlerConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(new ErrorMessage(ErrorCode.UNAUTHORIZED)));
            writer.flush();
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(new ErrorMessage(ErrorCode.FORBIDDEN)));
            writer.flush();
        };
    }
}
