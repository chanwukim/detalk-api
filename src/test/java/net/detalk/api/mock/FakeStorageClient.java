package net.detalk.api.mock;

import net.detalk.api.support.s3.StorageClient;

/**
 * @deprecated 이 클래스는 더 이상 사용되지 않으며 향후 제거될 예정
 * 카페인 캐시를 이용합니다
 */
@Deprecated(since = "0.2", forRemoval = true)
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
