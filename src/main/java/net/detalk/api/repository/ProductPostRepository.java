package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPost.PRODUCT_POST;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPost;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostRepository {

    private final DSLContext dsl;

    public ProductPost save(Long writerId, Long productId, Instant now) {
        return dsl.insertInto(PRODUCT_POST)
            .set(PRODUCT_POST.WRITER_ID, writerId)
            .set(PRODUCT_POST.PRODUCT_ID, productId)
            .set(PRODUCT_POST.CREATED_AT, now)
            .returning()
            .fetchOneInto(ProductPost.class);
    }

    public Optional<ProductPost> findById(Long id) {
        return dsl.selectFrom(PRODUCT_POST)
            .where(PRODUCT_POST.ID.eq(id))
            .fetchOptionalInto(ProductPost.class);
    }

}
