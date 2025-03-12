package net.detalk.api.auth.controller.v1;

import lombok.RequiredArgsConstructor;
import net.detalk.api.auth.controller.v1.response.GetSessionInfoResponse;
import net.detalk.api.auth.service.SessionOAuth2Service;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SessionOAuth2Service authService;

    @GetMapping("/session")
    public ResponseEntity<GetSessionInfoResponse> getSessionInfo(
        @HasRole({SecurityRole.MEMBER, SecurityRole.ADMIN}) SecurityUser user
    ) {
        GetSessionInfoResponse sessionInfo = authService.getSessionInfo(user.getId());
        return ResponseEntity.ok(sessionInfo);
    }

}
