package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.AttachmentFile;
import net.detalk.api.domain.UploadFileMetadata;
import net.detalk.api.domain.FileWithPresigned;
import net.detalk.api.repository.AttachmentFileRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.s3.StorageClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final AttachmentFileRepository attachmentFileRepository;
    private final StorageClient storageClient;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    public FileWithPresigned createPreSignedUrl(Long uploaderId, UploadFileMetadata uploadFileMetadata) {
        UUID fileId = uuidGenerator.generateV7();

        String baseKey = String.format("u/%s", fileId);
        String fileName = uploadFileMetadata.fileName();

        // 파일 이름과 확장자 분리
        String extension = null;
        String realFileName = fileName;

        int dotIndex = fileName.lastIndexOf('.');

        // 확장자가 있는 경우 (점이 있고, 점 뒤에 문자열이 있으면 확장자로 처리)
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1);
            realFileName = fileName.substring(0, dotIndex);
        }
        // 점으로 시작하는 파일은 확장자가 없다고 가정 (예: .gitignore, .prettierrc 등)
        else if (dotIndex == 0 && fileName.length() > 1) {
            extension = null;
            realFileName = fileName;
        }

        // Pre-signed URL 생성
        String preSignedUrl = storageClient.createPreSignedUrl(baseKey);
        String publicUrl = String.format("https://cdn.detalk.net/" + baseKey);

        AttachmentFile attachmentFile = attachmentFileRepository.save(
            AttachmentFile.builder()
                .id(fileId)
                .uploaderId(uploaderId)
                .name(realFileName)
                .extension(extension)
                .createdAt(timeHolder.now())
                .url(publicUrl)
                .build()
        );

        return FileWithPresigned.builder()
            .id(attachmentFile.getId())
            .url(publicUrl)
            .preSignedUrl(preSignedUrl)
            .build();
    }
}
