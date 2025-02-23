package net.detalk.api.product.repository;

import java.util.Optional;
import net.detalk.api.product.domain.ProductMaker;

public interface ProductMakerRepository {

    ProductMaker save(ProductMaker maker);

    Optional<ProductMaker> findByProductIdAndMemberId(Long productId, Long memberId);

    void deleteByProductIdAndMemberId(Long productId, Long memberId);
}
