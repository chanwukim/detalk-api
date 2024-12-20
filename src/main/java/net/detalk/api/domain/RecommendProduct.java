package net.detalk.api.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RecommendProduct {

    private Long id;
    private Long recommendId;
    private Long productPostId;
    private Long memberId;
    private Instant createdAt;

    @Builder
    public RecommendProduct(Long id, Long recommendId, Long productPostId, Long memberId,
        Instant createdAt) {
        this.id = id;
        this.recommendId = recommendId;
        this.productPostId = productPostId;
        this.memberId = memberId;
        this.createdAt = createdAt;
    }

}
