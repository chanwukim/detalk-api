package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AttachmentFile {
    private Long id;
    private Long uploaderId;
    private String name;
    private String extension;
    private String url;
    private Instant createdAt;

    @Builder
    public AttachmentFile(Long id, Long uploaderId, String name, String extension, String url, Instant createdAt) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.name = name;
        this.extension = extension;
        this.url = url;
        this.createdAt = createdAt;
    }
}
