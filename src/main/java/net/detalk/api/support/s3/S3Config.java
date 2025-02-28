package net.detalk.api.support.s3;

import lombok.RequiredArgsConstructor;
import net.detalk.api.image.service.ImageService;
import net.detalk.api.support.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * @deprecated
 * 클라우드 플레어 R2에서 Images로 변경함. {@link ImageService} 참고
 * @see <a href="https://github.com/chanwukim/detalk-api/issues/90">https://github.com/chanwukim/detalk-api/issues/90</a>
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final AppProperties appProperties;

    /**
     * R2
     * https://developers.cloudflare.com/r2/examples/aws/aws-sdk-java
     * 기타 참고
     * https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/java_s3_code_examples.html
     * https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/examples-s3.html
     */
    @Bean
    protected S3Client s3Client() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
            appProperties.getS3AccessKey(),
            appProperties.getS3SecretKey()
        );

        return S3Client.builder()
            .region(Region.of("auto"))
            .endpointOverride(URI.create(appProperties.getS3Endpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())
            .build();
    }

    @Bean
    protected S3Presigner s3Presigner() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
            appProperties.getS3AccessKey(),
            appProperties.getS3SecretKey()
        );
        return S3Presigner.builder()
            .endpointOverride(URI.create(appProperties.getS3Endpoint()))  // R2 엔드포인트
            .region(Region.of("auto"))  // R2는 region을 auto로 설정
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())
            .build();
    }
}
