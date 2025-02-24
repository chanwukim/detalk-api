package net.detalk.api.admin.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 방문자 로그 도메인
 */
@Getter
public class VisitorLog {

    private Long id;
    private String sessionId;     // 방문자의 세션 ID
    private String continentCode; // 방문자의 대륙 코드 (예: "NA")
    private String countryIso;    // 방문자의 국가 ISO 코드 (예: "US")
    private String countryName;   // 방문자의 국가명 (예: "United States")
    private LocalDateTime visitedAt; // 방문 시각
    private String userAgent;     // 브라우저 정보
    private String referer;       // 이전 페이지 정보

    @Builder
    public VisitorLog(Long id, String sessionId, String continentCode, String countryIso,
        String countryName, LocalDateTime visitedAt, String userAgent, String referer) {
        this.id = id;
        this.sessionId = sessionId;
        this.continentCode = continentCode;
        this.countryIso = countryIso;
        this.countryName = countryName;
        this.visitedAt = visitedAt;
        this.userAgent = userAgent;
        this.referer = referer;
    }
}
