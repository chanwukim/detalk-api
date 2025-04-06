package net.detalk.api.support.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.domain.exception.JwtTokenException;
import net.detalk.api.support.error.ErrorMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 예외 처리 필터
 * JWT 관련 예외를 캐치하여 적절한 오류 응답 생성
 *
 * 이 필터는 JwtAuthenticationFilter보다 먼저 실행되어
 * 필터 체인 전체를 try-catch 블록으로 감싸는 역할을 합니다.
 *
 * 필터 실행 순서:
 * 1. JwtExceptionFilter의 doFilterInternal() 시작
 * 2. try 블록 내에서 filterChain.doFilter() 호출
 * 3. JwtAuthenticationFilter 실행
 * 4. JwtAuthenticationFilter에서 예외 발생 시 JwtExceptionFilter의 catch 블록으로 전파
 * 5. 예외 처리 및 응답 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (JwtTokenException e) {
            log.debug("JWT 토큰 예외={},{}", e.getErrorCode(), e.getMessage());
            SecurityContextHolder.clearContext();

            response.setStatus(e.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorMessage errorMessage = new ErrorMessage(e.getErrorCode(), e.getMessage());
            objectMapper.writeValue(response.getOutputStream(), errorMessage);
        }
    }
}
