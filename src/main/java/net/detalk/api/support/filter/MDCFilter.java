package net.detalk.api.support.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import net.detalk.api.support.UUIDGenerator;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class MDCFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private final UUIDGenerator uuidGenerator;

    public MDCFilter(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    public void doFilter(ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = String.valueOf(uuidGenerator.generateV7());
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
