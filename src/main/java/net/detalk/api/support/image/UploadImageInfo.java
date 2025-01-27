package net.detalk.api.support.image;

import lombok.Builder;

@Builder
public record UploadImageInfo(
    String id,
    String uploadUrl,
    String imageUrl
) {}
