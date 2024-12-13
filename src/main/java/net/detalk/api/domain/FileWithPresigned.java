package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileWithPresigned {
    private Long id;
    private String url;
    private String preSignedUrl;

    @Builder
    public FileWithPresigned(Long id, String url, String preSignedUrl) {
        this.id = id;
        this.url = url;
        this.preSignedUrl = preSignedUrl;
    }
}
