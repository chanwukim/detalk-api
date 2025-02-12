package net.detalk.api.support.s3;

import net.detalk.api.service.ImageService;

/**
 * @deprecated
 * 클라우드 플레어 R2에서 Images로 변경함. {@link ImageService} 참고
 * @see <a href="https://github.com/chanwukim/detalk-api/issues/90">https://github.com/chanwukim/detalk-api/issues/90</a>
 */
public class S3Exception extends RuntimeException {
    public S3Exception(String message) {
        super(message);
    }
}
