package net.detalk.api.product.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.detalk.api.product.domain.ProductLink;
import net.detalk.api.product.repository.ProductLinkRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductLinkService {

    private final ProductLinkRepository productLinkRepository;

    public ProductLink getOrCreateProductLink(String url, Long productId, Instant now) {
        return productLinkRepository.findByUrl(url).orElseGet(
            () -> productLinkRepository.save(productId, url, now)
        );
    }
}
