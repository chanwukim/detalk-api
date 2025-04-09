package net.detalk.api.support.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ua_parser.Client;
import ua_parser.Parser;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInfoUtils {

    private final Parser parser;

    /**
     * 프록시 환경을 고려하여 클라이언트 IP 주소를 추출합니다.
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedForHeader) && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * User-Agent 문자열을 파싱하여 브라우저, OS, 기기 정보를 추출합니다.
     */
    public ClientAgentInfo getClientAgentInfo(HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("referer");
        System.out.println("referer = " + referer);

        if (!StringUtils.hasText(userAgent)) {
            return ClientAgentInfo.UNKNOWN_USER_AGENT(referer);
        }

        try {
            Client client = parser.parse(userAgent);
            return new ClientAgentInfo(
                client.userAgent.family, // 브라우저 패밀리 (e.g., "Chrome")
                client.os.family,        // OS 패밀리 (e.g., "Windows")
                client.device.family,     // 기기 패밀리 (e.g., "Other" for PC, "iPhone")
                userAgent,
                referer
            );
        } catch (Exception e) {
            log.warn("Failed to parse User-Agent string '{}': {}", userAgent, e.getMessage());
            return ClientAgentInfo.UNKNOWN_USER_AGENT(referer);
        }

    }

    /**
     * User-Agent 파싱 결과를 담는 내부 레코드
     */
    public record ClientAgentInfo(
        String browser,
        String os,
        String deviceType,
        String userAgent,
        String referrer
    ) {
        // 파싱 실패 또는 정보 없을 때 사용할 기본값
        public static ClientAgentInfo UNKNOWN_USER_AGENT(String referer) {
            return new ClientAgentInfo(
                "Unknown",
                "Unknown",
                "Unknown",
                "Unknown",
                referer
            );
        }
    }

}
