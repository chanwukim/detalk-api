package net.detalk.api.support.image;

import java.util.Map;

public interface ImageClient {
    UploadImageInfo createUploadUrl(Map<String, String> metadata);
}
