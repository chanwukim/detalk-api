package net.detalk.api.post.repository.impl;

import static net.detalk.jooq.tables.JProductPostSnapshotAttachmentFile.PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE;

import lombok.RequiredArgsConstructor;
import net.detalk.api.post.domain.ProductPostSnapshotAttachmentFile;
import net.detalk.api.post.repository.ProductPostSnapshotAttachmentFileRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostSnapshotAttachmentFileRepositoryImpl implements
    ProductPostSnapshotAttachmentFileRepository {

    private final DSLContext dsl;

    @Override
    public ProductPostSnapshotAttachmentFile save(ProductPostSnapshotAttachmentFile attachmentFile) {
        return dsl.insertInto(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE)
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID, attachmentFile.getSnapshotId())
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.ATTACHMENT_FILE_ID, attachmentFile.getAttachmentFileId())
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE, attachmentFile.getSequence())
            .returning()
            .fetchOneInto(ProductPostSnapshotAttachmentFile.class);
    }
}
