package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PreSignedData {
    private UUID id;
    private String path;
    private String preSignedUrl;

    @Builder
    public PreSignedData(UUID id, String path, String preSignedUrl) {
        this.id = id;
        this.path = path;
        this.preSignedUrl = preSignedUrl;
    }
}
