package net.detalk.api.product.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductLink {

    private Long id;
    private Long productId;
    private String url;
    private Instant createdAt;

    @Builder
    public ProductLink(Long id, Long productId, String url, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.url = url;
        this.createdAt = createdAt;
    }

}
