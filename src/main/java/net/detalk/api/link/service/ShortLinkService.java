package net.detalk.api.link.service;

import java.util.List;
import net.detalk.api.link.domain.CountryStatPoint;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.geo.domain.GeoInfo;
import net.detalk.api.geo.service.GeoIpLookupService;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.link.domain.ShortLink;
import net.detalk.api.link.domain.ShortLinkLog;
import net.detalk.api.link.domain.exception.ShortLinkCreationException;
import net.detalk.api.link.domain.exception.ShortLinkNotFoundException;
import net.detalk.api.link.repository.ShortLinkLogRepository;
import net.detalk.api.link.repository.ShortLinkRepository;
import net.detalk.api.link.util.ShortLinkGenerator;
import net.detalk.api.support.util.ClientInfoUtils.ClientAgentInfo;
import net.detalk.api.support.util.TimeHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private final ShortLinkLogRepository shortLinkLogRepository;
    private final ShortLinkGenerator shortLinkGenerator;
    private final TimeHolder timeHolder;
    private final AlarmSender alarmSender;
    private final GeoIpLookupService geoIpLookupService;
    private static final int MAX_CODE_GENERATION_RETRIES = 5;

    @Transactional
    public ShortLink createShortLink(String originalUrl, Long creatorId) {

        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_RETRIES; attempt++) {
            String shortCode = shortLinkGenerator.generate();
            try {
                Instant now = timeHolder.now();
                ShortLink shortLink = shortLinkRepository.save(shortCode, originalUrl, creatorId, now);
                log.info("[Link Created] Original: '{}', Code: {}, User: {}", originalUrl,
                    shortCode, creatorId);
                return shortLink;
            } catch (DuplicateKeyException e) {
                log.warn("[Link Creation Attempt {}/{}] Code collision for '{}'. Retrying...",
                    attempt, MAX_CODE_GENERATION_RETRIES, shortCode);
                 // 링크 생성 최대 횟수 도달 했을 시
                if (attempt == MAX_CODE_GENERATION_RETRIES) {
                    log.error("[Link Creation Failed] Max retries ({}) reached for URL: {}. Last code attempted: {}",
                        MAX_CODE_GENERATION_RETRIES, originalUrl, shortCode, e);
                    alarmSender.sendMessage("단축 URL 생성에 실패하였습니다. 이유 : 최대 횟수 초과");
                    throw new ShortLinkCreationException("Failed to generate unique short code after max retries.", e);
                }
            } catch (Exception e) {
                log.error("[Link Creation Failed] Database error for URL: {}. Code attempted: {}", originalUrl, shortCode, e);
                throw new ShortLinkCreationException("Failed to save link due to a database error.", e);
            }
        }
        log.error("[Link Creation Failed] Unexpected exit from generation loop for URL: {}", originalUrl);
        throw new ShortLinkCreationException("Unexpected error during short code generation.");
    }

    @Transactional
    public String findOriginalUrlAndRecordStats(String shortCode, String ip,
        ClientAgentInfo clientAgentInfo) {

        ShortLink shortLink = shortLinkRepository.findByShortCode(shortCode)
            .orElseThrow(ShortLinkNotFoundException::new);

        Instant now = timeHolder.now();

        GeoInfo geoInfo = geoIpLookupService.lookupGeoInfo(ip).orElse(null);

        ShortLinkLog shortLinkLog = ShortLinkLog.builder()
            .linkId(shortLink.getId())
            .clickedAt(now)
            .ipAddress(ip)
            .country(geoInfo != null ? geoInfo.countryName() : null)
            .city(geoInfo != null ? geoInfo.cityName() : null)
            .userAgent(clientAgentInfo.userAgent())
            .referrer(clientAgentInfo.referrer())
            .deviceType(clientAgentInfo.deviceType())
            .os(clientAgentInfo.os())
            .browser(clientAgentInfo.browser())
            .build();

        shortLinkLogRepository.save(shortLinkLog);

        return shortLink.getOriginalUrl();
    }

    /**
     * 국가별 단축 URL 클릭 횟수 조회
     * @param shortCode 단축 URL
     * @return 국가별 클릭 횟수
     */
    public List<CountryStatPoint> getClickStatsByCountry(String shortCode) {

        Long linkId = shortLinkRepository.findIdByShortCode(shortCode)
            .orElseThrow(ShortLinkNotFoundException::new);

        return shortLinkLogRepository.getClickCountsByLinkId(linkId);
    }
}
