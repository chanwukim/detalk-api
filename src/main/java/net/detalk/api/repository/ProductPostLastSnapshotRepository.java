package net.detalk.api.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPostLastSnapshot;
import net.detalk.api.domain.ProductPostSnapshot;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static net.detalk.jooq.tables.JProductPostLastSnapshot.PRODUCT_POST_LAST_SNAPSHOT;

@RequiredArgsConstructor
@Repository
public class ProductPostLastSnapshotRepository {

    private final DSLContext dsl;

    public ProductPostLastSnapshot save(Long postId, Long snapshotId) {
        return dsl.insertInto(PRODUCT_POST_LAST_SNAPSHOT)
            .set(PRODUCT_POST_LAST_SNAPSHOT.POST_ID, postId)
            .set(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID, snapshotId)
            .returning()
            .fetchOneInto(ProductPostLastSnapshot.class);
    }

    public Optional<ProductPostLastSnapshot> findByPostId(Long postId) {
        return dsl.selectFrom(PRODUCT_POST_LAST_SNAPSHOT)
            .where(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(postId))
            .fetchOptionalInto(ProductPostLastSnapshot.class);
    }

    public int update(Long postId, ProductPostSnapshot newSnapshot) {
        return dsl.update(PRODUCT_POST_LAST_SNAPSHOT)
            .set(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID, newSnapshot.getId())
            .where(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(postId))
            .execute();
    }
}
