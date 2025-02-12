package net.detalk.api.domain;

import lombok.Builder;

@Builder
public record UploadImageData(
    String id,
    String uploadUrl,
    String publicUrl
) {
}
