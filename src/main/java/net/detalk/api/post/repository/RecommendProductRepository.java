package net.detalk.api.post.repository;

import java.time.Instant;
import java.util.List;
import net.detalk.api.post.domain.RecommendProduct;

public interface RecommendProductRepository {

    boolean isAlreadyRecommended(Long memberId, Long recommendId, Long productPostId);

    RecommendProduct save(Long recommendId, Long productPostId, Long memberId, Instant now);

    void saveAll(List<RecommendProduct> recommendProducts);

}
