package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;
import net.detalk.api.service.ImageService;

import java.util.UUID;

/**
 * @deprecated
 * 클라우드 플레어 R2에서 Images로 변경함. {@link ImageService} 참고
 * @see <a href="https://github.com/chanwukim/detalk-api/issues/90">https://github.com/chanwukim/detalk-api/issues/90</a>
 */
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
