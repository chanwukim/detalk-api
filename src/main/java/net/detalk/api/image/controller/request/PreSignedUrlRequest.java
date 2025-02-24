package net.detalk.api.image.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
