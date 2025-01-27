package net.detalk.api.support.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.AppProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CloudflareImageClient implements ImageClient {
    private static final String UPLOAD_ENDPOINT = "/client/v4/accounts/{accountId}/images/v2/direct_upload";

    private final AppProperties appProperties;
    private final RestClient client;

    public CloudflareImageClient(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.client = RestClient.builder()
            .baseUrl("https://api.cloudflare.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getCloudFlareImagesApiToken())
            .build();
    }

    /**
     * @see <a href=
     * "https://developers.cloudflare.com/images/upload-images/direct-creator-upload/">https://developers.cloudflare.com/images/upload-images/direct-creator-upload/</a>
     */
    @Override
    public UploadImageInfo createUploadUrl(String uploaderId, String path, Map<String, String> metadata) {
        // metadata
        Map<String, Object> requestMetadata = new HashMap<>();

        requestMetadata.put("uploaderId", uploaderId);
        requestMetadata.put("path", path);

        if (metadata != null) {
            requestMetadata.putAll(metadata);
        }

        try {
            LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("requireSignedURLs", "true");
            formData.add("metadata", new ObjectMapper().writeValueAsString(requestMetadata));

            CloudflareImageResponse response = client.post()
                .uri(UPLOAD_ENDPOINT, appProperties.getCloudFlareAccountId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(formData)
                .retrieve()
                .body(CloudflareImageResponse.class);

            log.debug("[createUploadUrl] response {}", response);

            if (!response.success() || response.result() == null) {
                log.error("[createUploadUrl] Failed to create upload URL. Response: {}", response);
                throw new ImageUploadException("Failed to create upload URL");
            }

            CloudflareResult result = response.result();

            // client 이미지에 접근하는 url
            String imageDeliveryUrl = String.format(
                "https://cdn.detalk.net/images/%s/public",
                result.id);

            return UploadImageInfo.builder()
                .id(result.id)
                .uploadUrl(result.uploadURL)
                .imageUrl(imageDeliveryUrl)
                .build();
        } catch (Exception e) {
            log.error("Failed to create upload URL", e);
            throw new ImageUploadException("Failed to create upload URL");
        }
    }

    private record CloudflareImageResponse(
        boolean success,
        CloudflareResult result,
        List<String> errors,
        List<String> messages) {
    }

    private record CloudflareResult(
        String id,
        String uploadURL) {
    }
}
