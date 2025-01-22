package net.detalk.api.mock;

import net.detalk.api.support.s3.StorageClient;

public class FakeStorageClient implements StorageClient {

    @Override
    public String createPreSignedUrl(String objectKey) {
        return "http://fake.storage/" + objectKey;
    }

    // 테스트 내에서 URL 비교용 메서드
    public String getPreSignedUrl(String objectKey) {
        return "http://fake.storage/" + objectKey;
    }

}
