package net.detalk.api.image.controller.v1.response;

import lombok.Builder;

/**
 * 이미지 업로드 데이터 응답 DTO
 * @param id 업로드된 이미지 식별자
 * @param uploadUrl 업로드 URL
 * @param publicUrl 공개 URL
 */
@Builder
public record UploadImageData(
    String id,
    String uploadUrl,
    String publicUrl
) {
}
