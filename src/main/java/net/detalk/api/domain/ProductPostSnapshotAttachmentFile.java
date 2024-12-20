package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductPostSnapshotAttachmentFile {

    private Long id;
    private Long snapshotId;
    private Long attachmentFileId;
    private int sequence;

    @Builder
    public ProductPostSnapshotAttachmentFile(Long id, Long snapshotId, Long attachmentFileId,
        int sequence) {
        this.id = id;
        this.snapshotId = snapshotId;
        this.attachmentFileId = attachmentFileId;
        this.sequence = sequence;
    }

    public static ProductPostSnapshotAttachmentFile create(Long snapshotId, Long attachmentFileId,
        int sequence) {
        return ProductPostSnapshotAttachmentFile.builder()
            .snapshotId(snapshotId)
            .attachmentFileId(attachmentFileId)
            .sequence(sequence)
            .build();
    }

}
