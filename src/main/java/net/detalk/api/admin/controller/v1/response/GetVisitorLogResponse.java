package net.detalk.api.admin.controller.v1.response;

import java.time.LocalDateTime;

/**
 * 방문자 로그 정보 응답 DTO (주의 : 어드민 API 전용)
 * @param sessionId 세션 식별자
 * @param continentCode 대륙 코드
 * @param countryIso 국가 ISO 코드
 * @param countryName 국가 이름
 * @param visitedAt 방문 시간
 * @param userAgent 사용자 에이전트 (브라우저 정보 등)
 * @param referer 방문 경로 (이전 URL 주소)
 */
public record GetVisitorLogResponse(
    String sessionId,
    String continentCode,
    String countryIso,
    String countryName,
    LocalDateTime visitedAt,
    String userAgent,
    String referer
) {

}
