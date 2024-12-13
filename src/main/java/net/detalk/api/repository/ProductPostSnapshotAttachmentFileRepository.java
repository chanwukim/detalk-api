package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPostSnapshotAttachmentFile.PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE;

import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPostSnapshotAttachmentFile;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostSnapshotAttachmentFileRepository {

    private final DSLContext dsl;

    public ProductPostSnapshotAttachmentFile save(ProductPostSnapshotAttachmentFile attachmentFile) {
        return dsl.insertInto(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE)
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID, attachmentFile.getSnapshotId())
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.ATTACHMENT_FILE_ID, attachmentFile.getAttachmentFileId())
            .set(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE, attachmentFile.getSequence())
            .returning()
            .fetchOneInto(ProductPostSnapshotAttachmentFile.class);
    }
}
