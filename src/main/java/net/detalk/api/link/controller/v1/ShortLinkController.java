package net.detalk.api.link.controller.v1;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.link.controller.v1.request.CreateLinkRequest;
import net.detalk.api.link.controller.v1.response.CreateLinkResponse;
import net.detalk.api.link.service.ShortLinkService;
import net.detalk.api.support.config.AppProperties;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/short-links")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;
    private final AppProperties appProperties;

    @PostMapping
    public ResponseEntity<CreateLinkResponse> createShortLink(
        @Valid @RequestBody CreateLinkRequest request,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        String generatedCode = shortLinkService.createShortCode(request.originalUrl(), user.getId());

        String redirectPath = "/go/" + generatedCode;
        String baseUrl = appProperties.getBackendUrl().replaceFirst("/$", "");;
        URI location = URI.create(baseUrl + redirectPath);
        String fullShortUrl = location.toString();

        log.info("[API Res] Link created successfully: {} for original URL '{}'", fullShortUrl,
            request.originalUrl());

        CreateLinkResponse responseBody = CreateLinkResponse.builder()
            .shortUrl(fullShortUrl)
            .shortCode(generatedCode)
            .build();

        return ResponseEntity.created(location).body(responseBody);
    }
}
