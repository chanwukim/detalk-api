package net.detalk.api.post.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductPostSnapshotAttachmentFile {

    private Long id;
    private Long snapshotId;
    private UUID attachmentFileId;
    private int sequence;

    @Builder
    public ProductPostSnapshotAttachmentFile(Long id, Long snapshotId, UUID attachmentFileId,
        int sequence) {
        this.id = id;
        this.snapshotId = snapshotId;
        this.attachmentFileId = attachmentFileId;
        this.sequence = sequence;
    }

    public static ProductPostSnapshotAttachmentFile create(Long snapshotId, UUID attachmentFileId,
        int sequence) {
        return ProductPostSnapshotAttachmentFile.builder()
            .snapshotId(snapshotId)
            .attachmentFileId(attachmentFileId)
            .sequence(sequence)
            .build();
    }

}
