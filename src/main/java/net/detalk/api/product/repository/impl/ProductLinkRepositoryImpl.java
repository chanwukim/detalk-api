package net.detalk.api.product.repository.impl;

import static net.detalk.jooq.tables.JProductLink.PRODUCT_LINK;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.product.domain.ProductLink;
import net.detalk.api.product.repository.ProductLinkRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductLinkRepositoryImpl implements ProductLinkRepository {

    private final DSLContext dsl;

    @Override
    public Optional<ProductLink> findByUrl(String url) {
        return dsl.selectFrom(PRODUCT_LINK)
            .where(PRODUCT_LINK.URL.eq(url))
            .fetchOptionalInto(ProductLink.class);
    }

    @Override
    public ProductLink save(Long productId, String url, Instant now) {
        return dsl.insertInto(PRODUCT_LINK)
            .set(PRODUCT_LINK.PRODUCT_ID, productId)
            .set(PRODUCT_LINK.URL, url)
            .set(PRODUCT_LINK.CREATED_AT, now)
            .returning()
            .fetchOneInto(ProductLink.class);
    }

}
