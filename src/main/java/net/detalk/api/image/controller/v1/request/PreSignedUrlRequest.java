package net.detalk.api.image.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Pre-Signed URL 생성 요청 DTO
 * @param fileName 확장자가 포함된 파일 이름
 * @param fileType 파일 유형 (예: 이미지, 문서 등)
 * @param type 요청 타입
 */
public record PreSignedUrlRequest(
    /**
     * 확장자가 포함된 파일 이름
     */
    @NotBlank
    @Size(max = 255)
    String fileName,

    @Size(max = 16)
    String fileType,

    @NotBlank
    @Size(max = 255)
    String type
) {
}
