package net.detalk.api.link.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.link.domain.exception.LinkCreationException;
import net.detalk.api.link.domain.exception.LinkNotFoundException;
import net.detalk.api.link.repository.ShortLinkRepository;
import net.detalk.api.support.util.TimeHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;

    private final ShortLinkGenerator shortLinkGenerator;

    private final TimeHolder timeHolder;

    private final AlarmSender alarmSender;

    private static final int MAX_URL_GENERATION_RETRIES = 5;

    @Transactional
    public String createShortCode(String originalUrl, Long creatorId) {

        for (int attempt = 1; attempt <= MAX_URL_GENERATION_RETRIES; attempt++) {
            String shortCode = shortLinkGenerator.generate();
            try {
                Instant now = timeHolder.now();
                shortLinkRepository.save(shortCode, originalUrl, creatorId, now);
                log.info("[Link Created] Original: '{}', Code: {}, User: {}", originalUrl, shortCode, creatorId);
                return shortCode;
            } catch (DuplicateKeyException e) {
                log.warn("[Link Creation Attempt {}/{}] Code collision for '{}'. Retrying...",
                    attempt, MAX_URL_GENERATION_RETRIES, shortCode);
                if (attempt == MAX_URL_GENERATION_RETRIES) {
                    log.error("[Link Creation Failed] Max retries ({}) reached for URL: {}. Last code attempted: {}",
                        MAX_URL_GENERATION_RETRIES, originalUrl, shortCode, e);
                    alarmSender.sendMessage("최대 반복 횟수에 도달하여 단축 URL 생성에 실패했습니다.");
                    throw new LinkCreationException("Failed to generate unique short code after max retries.");
                }
            } catch (Exception e) {
                log.error("[Link Creation Failed] Database error for URL: {}. Code attempted: {}", originalUrl, shortCode, e);
                throw new LinkCreationException("Failed to save link due to a database error.");
            }
        }
        log.error("[Link Creation Failed] Unexpected exit from generation loop for URL: {}", originalUrl);
        throw new LinkCreationException("Unexpected error during short code generation.");
    }

    public String getOriginalUrl(String shortCode) {
        return shortLinkRepository.findOriginalUrlByShortCode(shortCode)
            .orElseThrow(() -> {
                log.warn("[Link Retrieval Failed] Code not found: {}", shortCode);
                return new LinkNotFoundException(shortCode);
            });
    }
}
