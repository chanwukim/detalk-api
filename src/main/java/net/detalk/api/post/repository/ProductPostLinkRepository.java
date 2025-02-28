package net.detalk.api.post.repository;

import net.detalk.api.post.domain.ProductPostLink;

public interface ProductPostLinkRepository {

    ProductPostLink save(Long postId, Long linkId);

}
