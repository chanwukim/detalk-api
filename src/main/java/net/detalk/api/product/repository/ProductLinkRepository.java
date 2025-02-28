package net.detalk.api.product.repository;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.product.domain.ProductLink;

public interface ProductLinkRepository {

    Optional<ProductLink> findByUrl(String url);

    ProductLink save(Long productId, String url, Instant now);

}
