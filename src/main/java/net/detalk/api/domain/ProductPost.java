package net.detalk.api.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductPost {

    private Long id;
    private Long writerId;
    private Long productId;
    private Instant createdAt;

    public ProductPost(Long id, Long writerId, Long productId, Instant createdAt) {
        this.id = id;
        this.writerId = writerId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

}
