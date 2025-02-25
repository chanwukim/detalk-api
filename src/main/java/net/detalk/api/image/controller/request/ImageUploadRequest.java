package net.detalk.api.image.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이미지 업로드 요청 DTO
 * @param fileName 이미지 파일명
 * @param purpose 이미지 용도 (예: profile, feed 등)
 */
public record ImageUploadRequest(
    /**
     * 이미지 파일명
     */
    @NotBlank
    @Size(max = 255)
    String fileName,

    /**
     * 이미지 용도 (예: profile, feed 등)
     */
    @NotBlank
    @Size(max = 50)
    String purpose
) {
}
