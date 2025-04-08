package net.detalk.api.link.repository;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.link.domain.ShortLink;

public interface ShortLinkRepository {

    /**
     * 단축 링크 정보를 저장합니다.
     * @param shortCode 생성된 고유 단축 코드 (Not Null)
     * @param originalUrl 원본 URL (Not Null)
     * @param creatorId 생성자 ID (Nullable)
     * @param createdAt 생성 시각 (Not Null) - Instant 타입
     * @throws org.springframework.dao.DuplicateKeyException shortCode가 중복될 경우
     */
    ShortLink save(String shortCode, String originalUrl, Long creatorId, Instant createdAt); // Instant 파라미터 추가

    /**
     * 단축 코드를 사용하여 원본 URL을 조회합니다.
     * @param shortCode 조회할 단축 코드 (Not Null)
     * @return 원본 URL Optional
     */
    Optional<String> findOriginalUrlByShortCode(String shortCode);


}
