package net.detalk.api.product.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import net.detalk.api.support.util.TimeHolder;

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

    public static ProductMaker create(Long productId, Long memberId, TimeHolder timeHolder) {
        return ProductMaker.builder()
            .productId(productId)
            .memberId(memberId)
            .createdAt(timeHolder.now())
            .build();
    }
}
