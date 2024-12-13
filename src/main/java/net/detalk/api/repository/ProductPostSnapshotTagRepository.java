package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPostSnapshotTag.PRODUCT_POST_SNAPSHOT_TAG;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPostSnapshotTag;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostSnapshotTagRepository {

    private final DSLContext dsl;

    public void saveAll(List<ProductPostSnapshotTag> snapshotTags) {
        var bulk = snapshotTags.stream()
            .map(snapshotTag -> dsl.insertInto(PRODUCT_POST_SNAPSHOT_TAG)
                .set(PRODUCT_POST_SNAPSHOT_TAG.POST_ID, snapshotTag.getPostId())
                .set(PRODUCT_POST_SNAPSHOT_TAG.TAG_ID, snapshotTag.getTagId())
            ).toList();
        dsl.batch(bulk).execute();
    }

}
