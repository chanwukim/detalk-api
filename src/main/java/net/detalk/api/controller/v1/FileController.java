package net.detalk.api.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.UploadFileMetadata;
import net.detalk.api.domain.FileWithPresigned;
import net.detalk.api.service.FileService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
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

    @PostMapping("/pre-signed")
    public ResponseEntity<FileWithPresigned> createPreSignedUrl(
        @HasRole(SecurityRole.MEMBER) SecurityUser user,
        @Valid @RequestBody UploadFileMetadata uploadFileMetadata
    ) {
        FileWithPresigned result = fileService.createPreSignedUrl(user.getId(), uploadFileMetadata);
        return ResponseEntity.ok().body(result);
    }
}
