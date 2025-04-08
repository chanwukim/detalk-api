package net.detalk.api.link.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * @param originalUrl 단축하고자 하는 원본 URL (http 또는 https 필수)
 */
public record CreateLinkRequest(
    @NotBlank(message = "Original URL cannot be blank.")
    @URL(message = "Invalid URL format. Must start with http:// or https://.")
    String originalUrl
) {}
