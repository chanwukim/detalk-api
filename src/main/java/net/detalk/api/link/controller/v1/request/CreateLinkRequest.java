package net.detalk.api.link.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * 단축 링크 생성 API 요청 DTO
 * @param originalUrl 단축하고자 하는 원본 URL (http 또는 https 필수)
 */
public record CreateLinkRequest(
    @NotBlank(message = "원본 URL을 입력해주세요.")
    // protocol 속성을 제거하여 http와 https 모두 허용하도록 수정
    @URL(message = "올바른 URL 형식이 아닙니다. (http:// 또는 https:// 로 시작해야 합니다.)")
    String originalUrl
) {}
