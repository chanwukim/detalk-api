package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.AttachmentFile;
import net.detalk.api.domain.UploadImageData;
import net.detalk.api.domain.exception.InvalidImageFormatException;
import net.detalk.api.repository.AttachmentFileRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.image.ImageClient;
import net.detalk.api.support.image.UploadImageInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private final AttachmentFileRepository attachmentFileRepository;
    private final ImageClient imageClient;
    private final UUIDGenerator uuidGenerator;
    private final TimeHolder timeHolder;

    public UploadImageData createImageUploadUrl(
        Long memberId,
        String fileName,
        String purpose
    ) {
        // 파일 이름과 확장자 분리
        String extension = null;
        String realFileName = fileName;

        int dotIndex = fileName.lastIndexOf('.');

        // 확장자가 있는 경우 (점이 있고, 점 뒤에 문자열이 있으면 확장자로 처리)
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
            realFileName = fileName.substring(0, dotIndex);
        }
        // 점으로 시작하는 파일은 확장자가 없다고 가정 (예: .gitignore, .prettierrc 등)
        else if (dotIndex == 0 && fileName.length() > 1) {
            extension = null;
            realFileName = fileName;
        }

        // 이미지 확장자 검증
        if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            log.info("[createImageUploadUrl] Invalid image extension: {}", extension);
            throw new InvalidImageFormatException("지원하지 않는 이미지 형식입니다");
        }

        UUID fileId = uuidGenerator.generateV7();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("memberId", memberId.toString());
        metadata.put("fileId", fileId.toString());
        metadata.put("fileName", fileName);
        metadata.put("purpose", purpose);

        UploadImageInfo uploadImageInfo = imageClient.createUploadUrl(metadata);
        log.debug("[createImageUploadUrl] uploadImageInfo = {}", uploadImageInfo);


        AttachmentFile file = AttachmentFile.builder()
            .id(fileId)
            .uploaderId(memberId)
            .name(realFileName)
            .extension(extension)
            .createdAt(timeHolder.now())
            .url(uploadImageInfo.imageUrl())
            .build();

        attachmentFileRepository.save(file);

        return UploadImageData.builder()
            .id(file.getId().toString())
            .uploadUrl(uploadImageInfo.uploadUrl())
            .publicUrl(uploadImageInfo.imageUrl())
            .build();
    }
}
