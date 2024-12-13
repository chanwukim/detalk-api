package net.detalk.api.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductMaker {

    private Long id;
    private Long productId;
    private Long memberId;
    private Instant createdAt;
    private Instant deletedAt;

    @Builder
    public ProductMaker(Long id, Long productId, Long memberId, Instant createdAt,
        Instant deletedAt) {
        this.id = id;
        this.productId = productId;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
}
