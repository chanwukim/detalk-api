package net.detalk.api.support.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties( prefix = "detalk")
public class AppProperties {
    private String baseUrl;

    private String tokenSecret;
    private long accessTokenExpiresInSeconds;
    private long refreshTokenExpiresInSeconds;

    private String s3Bucket;
    private String s3Endpoint;
    private String s3AccessKey;
    private String s3SecretKey;

    private int uploadUrlExpiryMinutes;
    private String cloudFlareImagesApiToken;
    private String cloudFlareAccountId;
    private String cloudFlareAccountHash;
}