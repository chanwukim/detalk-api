package net.detalk.api.support.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(2)
public class EndpointLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(EndpointLoggingFilter.class);
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String STATUS_CODE_KEY = "statusCode";
    private static final String SESSION_ENDPOINT = "/api/v1/auth/session";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String endpoint = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");

        boolean isNextJsRequest = userAgent != null && userAgent.contains("node");
        boolean isSessionRequest = SESSION_ENDPOINT.equals(endpoint);

        if (!isNextJsRequest || !isSessionRequest) {
            MDC.put(ENDPOINT_KEY, endpoint);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            int statusCode = response.getStatus();

            // Next.js 요청이 아니고 세션 엔드포인트 요청이 아닌 경우에만 로깅
            if (!isNextJsRequest && !isSessionRequest) {
                    MDC.put(STATUS_CODE_KEY, String.valueOf(statusCode));
                    log.info("EndPoint={} : StatusCode={}", endpoint, statusCode);
            }

            MDC.remove(ENDPOINT_KEY);
            MDC.remove(STATUS_CODE_KEY);
        }
    }
}
