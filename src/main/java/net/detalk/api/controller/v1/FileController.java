package net.detalk.api.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.UploadFileMetadata;
import net.detalk.api.domain.FileWithPresigned;
import net.detalk.api.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    // TODO: 인가 어노테이션
    @PostMapping("/pre-signed")
    public ResponseEntity<FileWithPresigned> createPreSignedUrl(@Valid @RequestBody UploadFileMetadata uploadFileMetadata) {
        Long memberId = 1L;
        FileWithPresigned result = fileService.createPreSignedUrl(memberId, uploadFileMetadata);
        return ResponseEntity.ok().body(result);
    }
}
