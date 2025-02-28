package net.detalk.api.post.domain;

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
    private Long recommendCount;

    public ProductPost(Long id, Long writerId, Long productId, Instant createdAt, Long recommendCount) {
        this.id = id;
        this.writerId = writerId;
        this.productId = productId;
        this.createdAt = createdAt;
        this.recommendCount = recommendCount;
    }

    public boolean isAuthor(Long loginMemberId) {
        return this.writerId.equals(loginMemberId);
    }

}
