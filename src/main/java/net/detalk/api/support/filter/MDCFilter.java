package net.detalk.api.support.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.util.UUIDGenerator;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * MDC(Mapped Diagnostic Context)를 설정하는 필터
 * - 모든 로그에 요청 정보를 포함하기 위해 가장 먼저 실행됨
 * - 요청 시작부터 종료까지 일관된 컨텍스트 정보 유지
 * - 요청 처리 완료 후 MDC 컨텍스트를 정리하여 메모리 누수 방지
 */
@Slf4j
@Order(1)
@RequiredArgsConstructor
@Component
public class MDCFilter implements Filter {

    // 클라이언트가 요청 보낼 때 고유한 요청 식별자를 전달하는 표준화된 헤더
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    // MDC에 저장할 ID KEY
    private static final String CORRELATION_ID_KEY = "correlationId";
    // MDC에 저장할 요청 엔드포인트
    private static final String ENDPOINT_KEY = "endpoint";
    // MDC에 저장할 HTTP 응답 상태 코드
    private static final String HTTP_STATUS_CODE_KEY = "httpStatusCode";

    // 로깅 제외할 항목
    private static final String SESSION_ENDPOINT = "/api/v1/auth/session";
    private static final String NEXT_JS_REQUEST = "node";

    // 로깅 메세지 상수
    private static final String HTTP_REQUEST_MSG = "Request completed!";
    private static final String NON_HTTP_REQUEST_MSG = "Request completed! (non-HTTP)";

    private final UUIDGenerator uuidGenerator;

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

        // 요청 고유 ID 없다면 직접 생성
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = uuidGenerator.generateV4().toString().substring(0, 8);
        }

        String userAgent = httpRequest.getHeader("User-Agent");
        String endpoint = httpRequest.getRequestURI();

        // Next.js SSR 요청이거나 세션 엔드포인트면 로깅 제외
        boolean isNextJsRequest = (userAgent != null && userAgent.contains(NEXT_JS_REQUEST));
        boolean isSessionEndpoint = SESSION_ENDPOINT.equals(endpoint);
        boolean shouldLog = !isNextJsRequest && !isSessionEndpoint;

        // 로깅 대상 : correlationId와 엔드포인트 로깅
        if (shouldLog) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
            MDC.put(ENDPOINT_KEY, endpoint);
        }

        try {
            chain.doFilter(request, response);
        } catch (Exception e){
            log.error("Request failed! endpoint={}, correlationId={}, error={}",
                endpoint, correlationId, e.getMessage());
            throw e;
        }
        finally {
            if (shouldLog) {
                // HTTP 요청일 경우
                if (response instanceof HttpServletResponse httpResponse) {
                    int statusCode = httpResponse.getStatus();
                    MDC.put(HTTP_STATUS_CODE_KEY, String.valueOf(statusCode));
                    log.info(HTTP_REQUEST_MSG);
                } else {
                    // HTTP 요청이 아닐 경우
                    log.info(NON_HTTP_REQUEST_MSG);
                }
            }
            MDC.clear();
        }
    }
}
