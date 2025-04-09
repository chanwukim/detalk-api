package net.detalk.api.link.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.link.controller.v1.response.ResolveShortLinkResponse;
import net.detalk.api.link.service.ShortLinkService;
import net.detalk.api.support.util.ClientInfoUtils;
import net.detalk.api.support.util.ClientInfoUtils.ClientAgentInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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