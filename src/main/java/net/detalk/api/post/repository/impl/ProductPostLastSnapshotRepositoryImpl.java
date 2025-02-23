package net.detalk.api.post.repository.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.post.domain.ProductPostLastSnapshot;
import net.detalk.api.post.domain.ProductPostSnapshot;
import net.detalk.api.post.repository.ProductPostLastSnapshotRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static net.detalk.jooq.tables.JProductPostLastSnapshot.PRODUCT_POST_LAST_SNAPSHOT;

@RequiredArgsConstructor
@Repository
public class ProductPostLastSnapshotRepositoryImpl implements ProductPostLastSnapshotRepository {

    private final DSLContext dsl;

    @Override
    public ProductPostLastSnapshot save(Long postId, Long snapshotId) {
        return dsl.insertInto(PRODUCT_POST_LAST_SNAPSHOT)
            .set(PRODUCT_POST_LAST_SNAPSHOT.POST_ID, postId)
            .set(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID, snapshotId)
            .returning()
            .fetchOneInto(ProductPostLastSnapshot.class);
    }

    @Override
    public Optional<ProductPostLastSnapshot> findByPostId(Long postId) {
        return dsl.selectFrom(PRODUCT_POST_LAST_SNAPSHOT)
            .where(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(postId))
            .fetchOptionalInto(ProductPostLastSnapshot.class);
    }

    @Override
    public int update(Long postId, ProductPostSnapshot newSnapshot) {
        return dsl.update(PRODUCT_POST_LAST_SNAPSHOT)
            .set(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID, newSnapshot.getId())
            .where(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(postId))
            .execute();
    }
}
