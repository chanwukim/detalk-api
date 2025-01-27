package net.detalk.api.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ImageUploadRequest(
    /**
     * 이미지 파일명
     */
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^.+\\.(jpg|jpeg|png|gif|webp)$", message = "지원되는 이미지 형식이 아닙니다")
    String fileName,

    /**
     * 이미지 용도 (예: profile, feed 등)
     */
    @NotBlank
    @Size(max = 50)
    String purpose
) {
}
