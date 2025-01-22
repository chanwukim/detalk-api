package net.detalk.api.mock;

import net.detalk.api.support.s3.StorageClient;

public class FakeStorageClient implements StorageClient {

    private static final String FAKE_STORAGE_URL = "http://fake.storage/";

    @Override
    public String createPreSignedUrl(String objectKey) {
        return FAKE_STORAGE_URL + objectKey;
    }

    // 테스트 내에서 URL 비교용 메서드
    public String getPreSignedUrl(String objectKey) {
        return FAKE_STORAGE_URL + objectKey;
    }

}
