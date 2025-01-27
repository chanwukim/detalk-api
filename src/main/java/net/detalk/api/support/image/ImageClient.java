package net.detalk.api.support.image;

import java.util.Map;

public interface ImageClient {
    UploadImageInfo createUploadUrl(String uploadIer, String path, Map<String, String> metadata);
}
