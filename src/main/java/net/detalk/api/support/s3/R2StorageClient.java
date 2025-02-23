package net.detalk.api.support.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.image.service.ImageService;
import net.detalk.api.support.AppProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/**
 * @deprecated
 * 클라우드 플레어 R2에서 Images로 변경함. {@link ImageService} 참고
 * @see <a href="https://github.com/chanwukim/detalk-api/issues/90">https://github.com/chanwukim/detalk-api/issues/90</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2StorageClient implements StorageClient {
    private final AppProperties appProperties;
    private final S3Presigner s3Presigner;

    @Override
    public String createPreSignedUrl(String objectKey) {
        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                // 10분간 유효한 URL
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(builder -> builder
                    .bucket(appProperties.getS3Bucket())
                    .key(objectKey)
                    .build())
                .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.debug("[createPresignedUrl] Presigned URL : {}", presignedUrl);

            return presignedRequest.url().toExternalForm();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new S3Exception(e.getMessage());
        }
    }
}
