package net.detalk.api.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadFileMetadata(
    /**
     * 확장자가 포함된 파일 이름
     */
    @NotBlank
    @Size(max = 255)
    String fileName,

    @Size(max = 16)
    String contentType
) {
}
