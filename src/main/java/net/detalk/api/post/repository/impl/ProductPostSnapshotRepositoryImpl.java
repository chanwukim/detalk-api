package net.detalk.api.post.repository.impl;

import static net.detalk.jooq.tables.JProductPostSnapshot.PRODUCT_POST_SNAPSHOT;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.post.domain.ProductPostSnapshot;
import net.detalk.api.post.repository.ProductPostSnapshotRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostSnapshotRepositoryImpl implements ProductPostSnapshotRepository {

    private final DSLContext dsl;

    @Override
    public ProductPostSnapshot save(ProductPostSnapshot snapshot) {
        return dsl.insertInto(PRODUCT_POST_SNAPSHOT)
            .set(PRODUCT_POST_SNAPSHOT.POST_ID, snapshot.getPostId())
            .set(PRODUCT_POST_SNAPSHOT.PRICING_PLAN_ID, snapshot.getPricingPlanId())
            .set(PRODUCT_POST_SNAPSHOT.TITLE, snapshot.getTitle())
            .set(PRODUCT_POST_SNAPSHOT.DESCRIPTION, snapshot.getDescription())
            .set(PRODUCT_POST_SNAPSHOT.CREATED_AT, snapshot.getCreatedAt())
            .returning()
            .fetchOneInto(ProductPostSnapshot.class);
    }

    @Override
    public Optional<ProductPostSnapshot> findById(Long id) {
        return dsl.selectFrom(PRODUCT_POST_SNAPSHOT)
            .where(PRODUCT_POST_SNAPSHOT.ID.eq(id))
            .fetchOptionalInto(ProductPostSnapshot.class);
    }
}
