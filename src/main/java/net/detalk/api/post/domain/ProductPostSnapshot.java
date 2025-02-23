package net.detalk.api.post.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductPostSnapshot {

    private Long id;
    private Long postId;
    private Long pricingPlanId;
    private String title;
    private String description;
    private Instant createdAt;

    @Builder
    public ProductPostSnapshot(Long id, Long postId, Long pricingPlanId, String title,
        String description, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.pricingPlanId = pricingPlanId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

}
