package net.detalk.api.repository;

import net.detalk.api.domain.AttachmentFile;

public interface AttachmentFileRepository {
    AttachmentFile save(AttachmentFile file);
}
