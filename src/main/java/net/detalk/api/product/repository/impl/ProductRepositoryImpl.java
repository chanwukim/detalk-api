package net.detalk.api.product.repository.impl;

import static net.detalk.jooq.tables.JProduct.PRODUCT;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.product.domain.Product;
import net.detalk.api.product.repository.ProductRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final DSLContext dsl;

    @Override
    public Optional<Product> findByName(String name) {
        return dsl.selectFrom(PRODUCT)
            .where(PRODUCT.NAME.eq(name))
            .fetchOptionalInto(Product.class);
    }

    @Override
    public Product save(String name, Instant now) {
        return dsl.insertInto(PRODUCT)
            .set(PRODUCT.NAME, name)
            .set(PRODUCT.CREATED_AT, now)
            .returning()
            .fetchOneInto(Product.class);
    }
}
