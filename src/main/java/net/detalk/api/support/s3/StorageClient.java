package net.detalk.api.support.s3;

public interface StorageClient {
    String createPreSignedUrl(String objectKey);
}
