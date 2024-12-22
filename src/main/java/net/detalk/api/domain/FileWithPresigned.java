package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FileWithPresigned {
    private UUID id;
    private String url;
    private String preSignedUrl;

    @Builder
    public FileWithPresigned(UUID id, String url, String preSignedUrl) {
        this.id = id;
        this.url = url;
        this.preSignedUrl = preSignedUrl;
    }
}
