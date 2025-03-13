package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import net.detalk.api.image.domain.PreSignedData;
import net.detalk.api.image.service.FileService;
import net.detalk.api.mock.FakeAttachmentFileRepository;
import net.detalk.api.mock.FakeStorageClient;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileServiceTest {

    @Deprecated
    private FileService fileService;
    private FakeAttachmentFileRepository fakeRepo;
    private FakeStorageClient fakeStorageClient;
    private FakeTimeHolder fakeTimeHolder;
    private FakeUUIDGenerator fakeUUIDGenerator;

    private final Instant fixedInstant = Instant.parse("2025-01-01T12:00:00Z");
    private final LocalDateTime fixedLocalDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    private final UUID fixedUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeAttachmentFileRepository();
        fakeStorageClient = new FakeStorageClient();
        fakeTimeHolder = new FakeTimeHolder(fixedInstant, fixedLocalDateTime);
        fakeUUIDGenerator = new FakeUUIDGenerator(fixedUUID);

        fileService = new FileService(fakeRepo, fakeStorageClient, fakeTimeHolder, fakeUUIDGenerator);
    }

    @Test
    void testCreatePreSignedUrl_WithExtension() {

        // given
        var uploaderId = 1L;
        var fileName = "sample.txt";
        var fileType = "image/png";
        var type = "user";

        // when
        PreSignedData preSignedUrl = fileService.createPreSignedUrl(uploaderId, fileName, fileType,
            type);

        // then
        assertThat(preSignedUrl.getId()).isEqualTo(fixedUUID);

        String expectedPath = String.format("%s/%s/%s", "image", type, fixedUUID);
        String expectedPreSignUrl = fakeStorageClient.getPreSignedUrl(expectedPath);
        assertThat(preSignedUrl.getPath()).isEqualTo(expectedPath);
        assertThat(preSignedUrl.getPreSignedUrl()).isEqualTo(expectedPreSignUrl);

    }

}