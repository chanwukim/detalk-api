package net.detalk.api.link.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.link.domain.exception.ShortLinkCreationException;
import net.detalk.api.link.domain.exception.ShortLinkNotFoundException;
import net.detalk.api.link.repository.ShortLinkRepository;
import net.detalk.api.link.util.ShortLinkGenerator;
import net.detalk.api.support.util.TimeHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private final ShortLinkGenerator shortLinkGenerator;
    private final TimeHolder timeHolder;
    private final AlarmSender alarmSender;
    private static final int MAX_CODE_GENERATION_RETRIES = 5;

    @Transactional
    public String createShortLink(String originalUrl, Long creatorId) {

        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_RETRIES; attempt++) {
            String shortCode = shortLinkGenerator.generate();
            try {
                Instant now = timeHolder.now();
                shortLinkRepository.save(shortCode, originalUrl, creatorId, now);
                log.info("[Link Created] Original: '{}', Code: {}, User: {}", originalUrl, shortCode, creatorId);
                return shortCode;
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
}
