package net.detalk.api.post.repository;

import java.util.Optional;
import net.detalk.api.post.domain.ProductPostLastSnapshot;
import net.detalk.api.post.domain.ProductPostSnapshot;

public interface ProductPostLastSnapshotRepository {

    ProductPostLastSnapshot save(Long postId, Long snapshotId);

    Optional<ProductPostLastSnapshot> findByPostId(Long postId);

    int update(Long postId, ProductPostSnapshot newSnapshot);

}
