package net.detalk.api.link.repository;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.link.domain.ShortLink;

public interface ShortLinkRepository {
    ShortLink save(String shortCode, String originalUrl, Long creatorId, Instant createdAt);
    Optional<String> findOriginalUrlByShortCode(String shortCode);
    Optional<ShortLink> findByShortCode(String shotCode);
}
