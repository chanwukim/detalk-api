package net.detalk.api.post.repository;

import java.util.Optional;
import net.detalk.api.post.domain.ProductPostSnapshot;

public interface ProductPostSnapshotRepository {

    ProductPostSnapshot save(ProductPostSnapshot snapshot);

    Optional<ProductPostSnapshot> findById(Long id);

}
