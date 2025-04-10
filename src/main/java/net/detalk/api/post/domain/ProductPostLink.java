package net.detalk.api.post.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductPostLink {

    private Long postId;
    private Long productLinkId;
    private Long shortLinkId;

    @Builder
    public ProductPostLink(Long postId, Long productLinkId, Long shortLinkId) {
        this.postId = postId;
        this.productLinkId = productLinkId;
        this.shortLinkId = shortLinkId;
    }

}
