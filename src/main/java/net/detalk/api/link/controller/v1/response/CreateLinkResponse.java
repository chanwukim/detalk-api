package net.detalk.api.link.controller.v1.response;

import lombok.Builder;

@Builder
public record CreateLinkResponse(
    String shortUrl,
    String shortCode
) {}