package net.detalk.api.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.request.ImageUploadRequest;
import net.detalk.api.domain.UploadImageData;
import net.detalk.api.service.ImageService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload-url")
    public ResponseEntity<UploadImageData> createImageUploadUrl (
        @HasRole({SecurityRole.MEMBER, SecurityRole.ADMIN}) SecurityUser user,
        @Valid @RequestBody ImageUploadRequest body
        ) {

        UploadImageData uploadImageData = imageService.createImageUploadUrl(
            user.getId(),
            body.fileName(),
            body.purpose()
        );

        return ResponseEntity
            .ok()
            .body(uploadImageData);
    }
}
