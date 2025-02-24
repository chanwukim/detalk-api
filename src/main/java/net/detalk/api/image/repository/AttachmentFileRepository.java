package net.detalk.api.image.repository;

import net.detalk.api.image.domain.AttachmentFile;

public interface AttachmentFileRepository {
    AttachmentFile save(AttachmentFile file);
}
