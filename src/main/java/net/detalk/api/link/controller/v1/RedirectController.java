package net.detalk.api.link.controller.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.link.domain.exception.LinkNotFoundException;
import net.detalk.api.link.service.ShortLinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortLinkService shortLinkService;

    /**
     * 제공된 shortCode에 해당하는 원본 URL로 리디렉션합니다.
     *
     * @param shortCode 리디렉션할 단축 코드
     * @return HTTP 302 리디렉션 응답 또는 404/500 오류 응답
     */
    @GetMapping("/go/{shortCode}")
    public ResponseEntity<Void> handleRedirect(@PathVariable("shortCode") String shortCode) {
        try {
            String originalUrl = shortLinkService.getOriginalUrl(shortCode);

            // TODO: 비동기 개선 - 클릭 통계 로깅 로직 추가
            log.debug("[Redirect] /go/{} -> {}", shortCode, originalUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                // 캐싱 방지 헤더
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .build();

        } catch (LinkNotFoundException e) {
            log.info("[Redirect Failed] Code not found: /go/{}", shortCode);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("[Redirect Failed] Error processing code: /go/{}", shortCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
