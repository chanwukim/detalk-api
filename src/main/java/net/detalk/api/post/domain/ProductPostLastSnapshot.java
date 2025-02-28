package net.detalk.api.post.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPostLastSnapshot {

    private Long postId;
    private Long snapShotId;

    public ProductPostLastSnapshot(Long postId, Long snapShotId) {
        this.postId = postId;
        this.snapShotId = snapShotId;
    }

}
