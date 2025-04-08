package net.detalk.api.link.controller.v1.response;

import lombok.Builder;

/**
 * 단축 링크 생성 API 성공 응답 DTO
 * @param shortUrl 생성된 전체 단축 URL (리디렉션 경로 포함)
 * @param shortCode 생성된 고유 단축 코드 부분
 */
@Builder
public record CreateLinkResponse(
    String shortUrl,
    String shortCode
) {}
