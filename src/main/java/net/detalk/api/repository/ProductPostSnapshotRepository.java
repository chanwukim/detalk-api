package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPostSnapshot.PRODUCT_POST_SNAPSHOT;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPostSnapshot;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostSnapshotRepository {

    private final DSLContext dsl;

    public ProductPostSnapshot save(Long postId, Long pricingPlanId, String title, String description,
        Instant now) {
        return dsl.insertInto(PRODUCT_POST_SNAPSHOT)
            .set(PRODUCT_POST_SNAPSHOT.POST_ID, postId)
            .set(PRODUCT_POST_SNAPSHOT.PRICING_PLAN_ID, pricingPlanId)
            .set(PRODUCT_POST_SNAPSHOT.TITLE, title)
            .set(PRODUCT_POST_SNAPSHOT.DESCRIPTION, description)
            .set(PRODUCT_POST_SNAPSHOT.CREATED_AT, now)
            .returning()
            .fetchOneInto(ProductPostSnapshot.class);
    }

    public Optional<ProductPostSnapshot> findById(Long id) {
        return dsl.selectFrom(PRODUCT_POST_SNAPSHOT)
            .where(PRODUCT_POST_SNAPSHOT.ID.eq(id))
            .fetchOptionalInto(ProductPostSnapshot.class);
    }

}
