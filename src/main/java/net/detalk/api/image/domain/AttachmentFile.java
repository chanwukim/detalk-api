package net.detalk.api.image.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class AttachmentFile {
    private UUID id;
    private Long uploaderId;
    private String name;
    private String extension;
    private String url;
    private Instant createdAt;

    @Builder
    public AttachmentFile(UUID id, Long uploaderId, String name, String extension, String url, Instant createdAt) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.name = name;
        this.extension = extension;
        this.url = url;
        this.createdAt = createdAt;
    }
}
