package net.detalk.api.link.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.link.controller.v1.request.CreateShortLinkRequest;
import net.detalk.api.link.controller.v1.response.CreateShortLinkResponse;
import net.detalk.api.link.controller.v1.response.ResolveShortLinkResponse;
import net.detalk.api.link.service.ShortLinkService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import net.detalk.api.support.util.ClientInfoUtils;
import net.detalk.api.support.util.ClientInfoUtils.ClientAgentInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 단축 링크 생성 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/short-links")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;
    private final ClientInfoUtils clientInfoUtils;
    @PostMapping
    public ResponseEntity<CreateShortLinkResponse> createShortLink(
        @Valid @RequestBody CreateShortLinkRequest request,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {

        String generatedCode = shortLinkService.createShortLink(request.originalUrl(), user.getId());
        log.info("[API Res] Link identifier created: {} for original URL '{}'", generatedCode, request.originalUrl());

        CreateShortLinkResponse responseBody = new CreateShortLinkResponse(generatedCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ResolveShortLinkResponse> resolveShortLink(
        @PathVariable("shortCode") String shortCode,
        HttpServletRequest servlet
    ) {

        ClientAgentInfo clientAgentInfo = clientInfoUtils.getClientAgentInfo(servlet);

        String ip = clientInfoUtils.getClientIpAddress(servlet);

        String originalUrl = shortLinkService.findOriginalUrlAndRecordStats(shortCode, ip,
            clientAgentInfo);

        ResolveShortLinkResponse responseBody = new ResolveShortLinkResponse(originalUrl);

        return ResponseEntity.ok().body(responseBody);
    }

}