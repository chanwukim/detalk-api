package net.detalk.api.product.repository;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.product.domain.Product;

public interface ProductRepository {

    Optional<Product> findByName(String name);

    Product save(String name, Instant now);

}
