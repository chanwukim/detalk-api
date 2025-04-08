package net.detalk.api.link.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ShortLink {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private Long creatorId;
    private Instant createdAt;

    @Builder
    public ShortLink(Long id, String shortCode, String originalUrl, Long creatorId,
        Instant createdAt) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }
}
