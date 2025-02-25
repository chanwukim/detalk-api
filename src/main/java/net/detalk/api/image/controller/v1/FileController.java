package net.detalk.api.image.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.detalk.api.image.controller.v1.request.PreSignedUrlRequest;
import net.detalk.api.image.domain.PreSignedData;
import net.detalk.api.image.service.FileService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/signed-url")
    public ResponseEntity<PreSignedData> createPreSignedUrl(
        @HasRole(SecurityRole.MEMBER) SecurityUser user,
        @Valid @RequestBody PreSignedUrlRequest body
        ) {
        PreSignedData result = fileService.createPreSignedUrl(user.getId(), body.fileName(), body.fileType(), body.type());
        return ResponseEntity.ok().body(result);
    }
}
