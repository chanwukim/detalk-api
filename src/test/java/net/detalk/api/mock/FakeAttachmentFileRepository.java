package net.detalk.api.mock;

import net.detalk.api.domain.AttachmentFile;
import net.detalk.api.repository.AttachmentFileRepository;

public class FakeAttachmentFileRepository implements AttachmentFileRepository {

    private AttachmentFile savedFile;

    @Override
    public AttachmentFile save(AttachmentFile file) {
        this.savedFile = file;
        return file;
    }

    public AttachmentFile getSavedFile() {
        return savedFile;
    }
}
