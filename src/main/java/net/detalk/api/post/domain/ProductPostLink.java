package net.detalk.api.post.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPostLink {

    private Long postId;
    private Long linkId;

    public ProductPostLink(Long postId, Long linkId) {
        this.postId = postId;
        this.linkId = linkId;
    }

}
