package net.detalk.api.image.repository.impl;

import lombok.RequiredArgsConstructor;
import net.detalk.api.image.domain.AttachmentFile;
import net.detalk.api.image.repository.AttachmentFileRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static net.detalk.jooq.Tables.ATTACHMENT_FILE;

@Repository
@RequiredArgsConstructor
public class AttachmentFileRepositoryImpl implements AttachmentFileRepository {
    private final DSLContext dsl;

    @Override
    public AttachmentFile save(AttachmentFile file) {
        return dsl.insertInto(ATTACHMENT_FILE)
            .set(ATTACHMENT_FILE.ID, file.getId())
            .set(ATTACHMENT_FILE.UPLOADER_ID, file.getUploaderId())
            .set(ATTACHMENT_FILE.NAME, file.getName())
            .set(ATTACHMENT_FILE.EXTENSION, file.getExtension())
            .set(ATTACHMENT_FILE.URL, file.getUrl())
            .set(ATTACHMENT_FILE.CREATED_AT, file.getCreatedAt())
            .returning()
            .fetchOneInto(AttachmentFile.class);
    }
}
