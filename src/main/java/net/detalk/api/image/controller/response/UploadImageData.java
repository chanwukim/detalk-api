package net.detalk.api.image.controller.response;

import lombok.Builder;

@Builder
public record UploadImageData(
    String id,
    String uploadUrl,
    String publicUrl
) {
}
