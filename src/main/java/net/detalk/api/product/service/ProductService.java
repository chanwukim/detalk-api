package net.detalk.api.product.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.detalk.api.product.domain.Product;
import net.detalk.api.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product getOrCreateProduct(String name, Instant now) {
        return productRepository.findByName(name)
            .orElseGet(() -> create(name, now));
    }

    public Product create(String name, Instant now) {
        return productRepository.save(name, now);
    }

}
