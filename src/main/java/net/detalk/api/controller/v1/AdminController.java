package net.detalk.api.controller.v1;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.response.ActiveSessionResponse;
import net.detalk.api.service.SessionTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SessionTrackingService sessionTrackingService;

    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionResponse>> getActiveSessions() {
        List<ActiveSessionResponse> activeSessions = sessionTrackingService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }


}
